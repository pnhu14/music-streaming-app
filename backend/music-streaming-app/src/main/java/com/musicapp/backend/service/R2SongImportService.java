package com.musicapp.backend.service;

import com.musicapp.backend.dto.R2SongImportItemResponse;
import com.musicapp.backend.dto.R2SongImportResponse;
import com.musicapp.backend.dto.SongDto;
import com.musicapp.backend.entity.MediaAsset;
import com.musicapp.backend.entity.R2ImportJob;
import com.musicapp.backend.entity.R2ImportJobItem;
import com.musicapp.backend.entity.Song;
import com.musicapp.backend.mapper.SongMapper;
import com.musicapp.backend.repository.MediaAssetRepository;
import com.musicapp.backend.repository.R2ImportJobItemRepository;
import com.musicapp.backend.repository.R2ImportJobRepository;
import com.musicapp.backend.repository.SongRepository;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class R2SongImportService {

  private static final String STORAGE_PROVIDER = "CLOUDFLARE_R2";
  private static final String IMPORT_SOURCE = "R2_IMPORT";
  private static final String ASSET_TYPE_AUDIO = "AUDIO";
  private static final String STATUS_AVAILABLE = "AVAILABLE";
  private static final String STATUS_DELETED = "DELETED";
  private static final String JOB_STATUS_RUNNING = "RUNNING";
  private static final String JOB_STATUS_COMPLETED = "COMPLETED";
  private static final String JOB_STATUS_COMPLETED_WITH_ERRORS = "COMPLETED_WITH_ERRORS";
  private static final String ITEM_STATUS_IMPORTED = "IMPORTED";
  private static final String ITEM_STATUS_SKIPPED = "SKIPPED";
  private static final String ITEM_STATUS_FAILED = "FAILED";
  private static final int DEFAULT_DURATION_SECONDS = 1;
  private static final Set<String> AUDIO_EXTENSIONS =
      Set.of(".mp3", ".wav", ".ogg", ".m4a", ".flac", ".aac");
  private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
  private static final Pattern NON_SLUG_CHARS = Pattern.compile("[^a-z0-9]+");

  private final R2StorageService r2StorageService;
  private final SongRepository songRepository;
  private final MediaAssetRepository mediaAssetRepository;
  private final R2ImportJobRepository r2ImportJobRepository;
  private final R2ImportJobItemRepository r2ImportJobItemRepository;

  @Transactional
  public R2SongImportResponse importSongs(String prefix) {
    String bucketName = r2StorageService.getBucketName();
    String normalizedPrefix = blankToNull(prefix);
    R2ImportJob job = createJob(bucketName, normalizedPrefix);
    List<R2ObjectSummary> objects = r2StorageService.listObjects(prefix);
    List<SongDto> importedSongs = new ArrayList<>();
    List<R2SongImportItemResponse> items = new ArrayList<>();
    int skippedObjects = 0;
    int failedObjects = 0;

    for (R2ObjectSummary object : objects) {
      ImportResult result = importObject(bucketName, object);
      saveJobItem(job, bucketName, object, result);
      items.add(
          new R2SongImportItemResponse(
              object.key(),
              result.status(),
              result.reason(),
              result.songId(),
              result.mediaAssetId()));

      if (ITEM_STATUS_IMPORTED.equals(result.status())) {
        importedSongs.add(result.song());
      } else if (ITEM_STATUS_SKIPPED.equals(result.status())) {
        skippedObjects++;
      } else {
        failedObjects++;
      }
    }

    finishJob(job, objects.size(), importedSongs.size(), skippedObjects, failedObjects);
    return new R2SongImportResponse(
        job.getId(),
        job.getStatus(),
        objects.size(),
        importedSongs.size(),
        skippedObjects,
        failedObjects,
        importedSongs,
        items);
  }

  private R2ImportJob createJob(String bucketName, String prefix) {
    R2ImportJob job = new R2ImportJob();
    job.setBucketName(bucketName);
    job.setPrefix(prefix);
    job.setStatus(JOB_STATUS_RUNNING);
    return r2ImportJobRepository.save(job);
  }

  private ImportResult importObject(String bucketName, R2ObjectSummary object) {
    String key = object.key();
    if (!isAudioKey(key)) {
      return ImportResult.skipped("not an audio object");
    }

    try {
      MediaAsset mediaAsset = upsertAudioAsset(bucketName, object);
      String publicUrl = mediaAsset.getPublicUrl();

      if (mediaAsset.getId() != null
          && songRepository.existsByAudioAsset_IdAndDeletedAtIsNull(mediaAsset.getId())) {
        UUID songId =
            songRepository
                .findByAudioAsset_IdAndDeletedAtIsNull(mediaAsset.getId())
                .map(song -> updateImportedSongReference(song, mediaAsset, publicUrl))
                .orElse(null);
        return ImportResult.skipped(
            "song already exists for media asset", songId, mediaAsset.getId());
      }

      Optional<Song> existingSong = songRepository.findByAudioUrlAndDeletedAtIsNull(key);
      if (existingSong.isPresent()) {
        Song song = existingSong.get();
        updateImportedSongReference(song, mediaAsset, publicUrl);
        return ImportResult.skipped(
            "song already exists for object key", song.getId(), mediaAsset.getId());
      }

      Optional<Song> existingPublicSong =
          songRepository.findByAudioUrlAndDeletedAtIsNull(publicUrl);
      if (existingPublicSong.isPresent()) {
        Song song = existingPublicSong.get();
        updateImportedSongReference(song, mediaAsset, publicUrl);
        return ImportResult.skipped(
            "song already exists for public url", song.getId(), mediaAsset.getId());
      }

      Song song = new Song();
      song.setTitle(resolveTitle(key));
      song.setSlug(resolveUniqueSlug(song.getTitle()));
      song.setDurationSeconds(DEFAULT_DURATION_SECONDS);
      song.setReleaseDate(LocalDate.now());
      song.setAudioUrl(publicUrl);
      song.setAudioAsset(mediaAsset);
      song.setExplicit(false);
      song.setStatus("PUBLISHED");
      Song savedSong = songRepository.save(song);
      return ImportResult.imported(
          SongMapper.toDto(savedSong), savedSong.getId(), mediaAsset.getId());
    } catch (RuntimeException exception) {
      return ImportResult.failed(shortMessage(exception));
    }
  }

  private MediaAsset upsertAudioAsset(String bucketName, R2ObjectSummary object) {
    MediaAsset mediaAsset =
        mediaAssetRepository
            .findByStorageProviderAndBucketNameAndStorageKeyAndAssetTypeAndStatusNot(
                STORAGE_PROVIDER, bucketName, object.key(), ASSET_TYPE_AUDIO, STATUS_DELETED)
            .orElseGet(MediaAsset::new);

    mediaAsset.setAssetType(ASSET_TYPE_AUDIO);
    mediaAsset.setStorageProvider(STORAGE_PROVIDER);
    mediaAsset.setBucketName(bucketName);
    mediaAsset.setStorageKey(object.key());
    mediaAsset.setPublicUrl(r2StorageService.createPublicUrl(object.key()));
    mediaAsset.setMimeType(resolveMimeType(object.key()));
    mediaAsset.setFileSizeBytes(object.size());
    mediaAsset.setChecksum(trimToNull(object.eTag()));
    mediaAsset.setEtag(trimToNull(object.eTag()));
    mediaAsset.setDurationSeconds(DEFAULT_DURATION_SECONDS);
    mediaAsset.setStatus(STATUS_AVAILABLE);
    mediaAsset.setLastModifiedAt(object.lastModified());
    mediaAsset.setImportedAt(Instant.now());
    mediaAsset.setImportSource(IMPORT_SOURCE);
    return mediaAssetRepository.save(mediaAsset);
  }

  private UUID updateImportedSongReference(Song song, MediaAsset mediaAsset, String publicUrl) {
    boolean changed = false;
    if (song.getAudioAsset() == null || !mediaAsset.getId().equals(song.getAudioAsset().getId())) {
      song.setAudioAsset(mediaAsset);
      changed = true;
    }
    if (!publicUrl.equals(song.getAudioUrl())) {
      song.setAudioUrl(publicUrl);
      changed = true;
    }
    if (changed) {
      songRepository.save(song);
    }
    return song.getId();
  }

  private void saveJobItem(
      R2ImportJob job, String bucketName, R2ObjectSummary object, ImportResult result) {
    R2ImportJobItem item = new R2ImportJobItem();
    item.setJob(job);
    item.setBucketName(bucketName);
    item.setObjectKey(object.key());
    item.setObjectSizeBytes(object.size());
    item.setObjectLastModifiedAt(object.lastModified());
    item.setEtag(trimToNull(object.eTag()));
    item.setStatus(result.status());
    item.setReason(result.reason());
    item.setSong(result.songId() == null ? null : songRepository.getReferenceById(result.songId()));
    item.setMediaAsset(
        result.mediaAssetId() == null
            ? null
            : mediaAssetRepository.getReferenceById(result.mediaAssetId()));
    r2ImportJobItemRepository.save(item);
  }

  private void finishJob(
      R2ImportJob job,
      int scannedObjects,
      int importedSongs,
      int skippedObjects,
      int failedObjects) {
    job.setScannedObjects(scannedObjects);
    job.setImportedSongs(importedSongs);
    job.setSkippedObjects(skippedObjects);
    job.setFailedObjects(failedObjects);
    job.setStatus(failedObjects > 0 ? JOB_STATUS_COMPLETED_WITH_ERRORS : JOB_STATUS_COMPLETED);
    job.setFinishedAt(Instant.now());
    r2ImportJobRepository.save(job);
  }

  private boolean isAudioKey(String key) {
    if (!StringUtils.hasText(key)) {
      return false;
    }

    String normalized = key.toLowerCase(Locale.ROOT);
    return AUDIO_EXTENSIONS.stream().anyMatch(normalized::endsWith);
  }

  private String resolveMimeType(String key) {
    String normalized = key.toLowerCase(Locale.ROOT);
    if (normalized.endsWith(".mp3")) {
      return "audio/mpeg";
    }
    if (normalized.endsWith(".wav")) {
      return "audio/wav";
    }
    if (normalized.endsWith(".ogg")) {
      return "audio/ogg";
    }
    if (normalized.endsWith(".m4a")) {
      return "audio/mp4";
    }
    if (normalized.endsWith(".flac")) {
      return "audio/flac";
    }
    if (normalized.endsWith(".aac")) {
      return "audio/aac";
    }

    return "application/octet-stream";
  }

  private String resolveTitle(String key) {
    String fileName = key;
    int slashIndex = fileName.lastIndexOf('/');
    if (slashIndex >= 0) {
      fileName = fileName.substring(slashIndex + 1);
    }

    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0) {
      fileName = fileName.substring(0, dotIndex);
    }

    String title = fileName.replace('_', ' ').replace('-', ' ').trim();
    return StringUtils.hasText(title) ? title : key;
  }

  private String resolveUniqueSlug(String title) {
    String baseSlug = toSlug(title);
    String slug = baseSlug;
    int suffix = 2;
    while (songRepository.existsBySlugAndDeletedAtIsNull(slug)) {
      slug = baseSlug + "-" + suffix;
      suffix++;
    }
    return slug;
  }

  private String toSlug(String value) {
    String normalized =
        DIACRITICS
            .matcher(Normalizer.normalize(value, Normalizer.Form.NFD))
            .replaceAll("")
            .toLowerCase(Locale.ROOT);
    String slug = NON_SLUG_CHARS.matcher(normalized).replaceAll("-").replaceAll("(^-|-$)", "");
    return StringUtils.hasText(slug) ? slug : "song";
  }

  private String blankToNull(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private String trimToNull(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private String shortMessage(RuntimeException exception) {
    String message =
        StringUtils.hasText(exception.getMessage())
            ? exception.getMessage()
            : exception.getClass().getSimpleName();
    return message.length() <= 255 ? message : message.substring(0, 255);
  }

  private record ImportResult(
      String status, String reason, SongDto song, UUID songId, UUID mediaAssetId) {

    private static ImportResult imported(SongDto song, UUID songId, UUID mediaAssetId) {
      return new ImportResult(ITEM_STATUS_IMPORTED, "imported", song, songId, mediaAssetId);
    }

    private static ImportResult skipped(String reason) {
      return skipped(reason, null, null);
    }

    private static ImportResult skipped(String reason, UUID songId, UUID mediaAssetId) {
      return new ImportResult(ITEM_STATUS_SKIPPED, reason, null, songId, mediaAssetId);
    }

    private static ImportResult failed(String reason) {
      return new ImportResult(ITEM_STATUS_FAILED, reason, null, null, null);
    }
  }
}
