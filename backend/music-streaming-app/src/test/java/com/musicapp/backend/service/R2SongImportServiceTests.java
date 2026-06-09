package com.musicapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.musicapp.backend.dto.R2SongImportResponse;
import com.musicapp.backend.entity.MediaAsset;
import com.musicapp.backend.entity.R2ImportJob;
import com.musicapp.backend.entity.R2ImportJobItem;
import com.musicapp.backend.entity.Song;
import com.musicapp.backend.repository.MediaAssetRepository;
import com.musicapp.backend.repository.R2ImportJobItemRepository;
import com.musicapp.backend.repository.R2ImportJobRepository;
import com.musicapp.backend.repository.SongRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class R2SongImportServiceTests {

  @Mock private R2StorageService r2StorageService;

  @Mock private SongRepository songRepository;

  @Mock private MediaAssetRepository mediaAssetRepository;

  @Mock private R2ImportJobRepository r2ImportJobRepository;

  @Mock private R2ImportJobItemRepository r2ImportJobItemRepository;

  @InjectMocks private R2SongImportService r2SongImportService;

  @Test
  void importSongsCreatesSongAssetAndJobItemForAudioObject() {
    UUID assetId = UUID.randomUUID();
    UUID songId = UUID.randomUUID();
    UUID jobId = UUID.randomUUID();
    R2ObjectSummary object =
        new R2ObjectSummary(
            "imports/Pulse Runner.wav", 1024L, Instant.parse("2026-05-29T12:00:00Z"), "etag-1");
    String publicUrl = "https://pub-example.r2.dev/imports/Pulse%20Runner.wav";

    when(r2StorageService.getBucketName()).thenReturn("music-streaming-audio");
    when(r2StorageService.listObjects("imports/")).thenReturn(List.of(object));
    when(r2StorageService.createPublicUrl(object.key())).thenReturn(publicUrl);
    when(r2ImportJobRepository.save(any(R2ImportJob.class)))
        .thenAnswer(
            invocation -> {
              R2ImportJob job = invocation.getArgument(0);
              if (job.getId() == null) {
                job.setId(jobId);
              }
              return job;
            });
    when(mediaAssetRepository
            .findByStorageProviderAndBucketNameAndStorageKeyAndAssetTypeAndStatusNot(
                anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(Optional.empty());
    when(mediaAssetRepository.save(any(MediaAsset.class)))
        .thenAnswer(
            invocation -> {
              MediaAsset mediaAsset = invocation.getArgument(0);
              mediaAsset.setId(assetId);
              return mediaAsset;
            });
    when(songRepository.findByAudioUrlAndDeletedAtIsNull(object.key()))
        .thenReturn(Optional.empty());
    when(songRepository.findByAudioUrlAndDeletedAtIsNull(publicUrl)).thenReturn(Optional.empty());
    when(songRepository.existsByAudioAsset_IdAndDeletedAtIsNull(assetId)).thenReturn(false);
    when(songRepository.existsBySlugAndDeletedAtIsNull("pulse-runner")).thenReturn(false);
    when(songRepository.save(any(Song.class)))
        .thenAnswer(
            invocation -> {
              Song song = invocation.getArgument(0);
              song.setId(songId);
              return song;
            });
    when(r2ImportJobItemRepository.save(any(R2ImportJobItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    R2SongImportResponse response = r2SongImportService.importSongs("imports/");

    assertThat(response.jobId()).isEqualTo(jobId);
    assertThat(response.status()).isEqualTo("COMPLETED");
    assertThat(response.scannedObjects()).isEqualTo(1);
    assertThat(response.importedSongs()).isEqualTo(1);
    assertThat(response.skippedObjects()).isZero();
    assertThat(response.failedObjects()).isZero();
    assertThat(response.songs()).extracting("title").containsExactly("Pulse Runner");
    assertThat(response.items()).extracting("status").containsExactly("IMPORTED");

    ArgumentCaptor<MediaAsset> mediaAssetCaptor = ArgumentCaptor.forClass(MediaAsset.class);
    verify(mediaAssetRepository).save(mediaAssetCaptor.capture());
    assertThat(mediaAssetCaptor.getValue().getStorageProvider()).isEqualTo("CLOUDFLARE_R2");
    assertThat(mediaAssetCaptor.getValue().getBucketName()).isEqualTo("music-streaming-audio");
    assertThat(mediaAssetCaptor.getValue().getStorageKey()).isEqualTo(object.key());
    assertThat(mediaAssetCaptor.getValue().getPublicUrl()).isEqualTo(publicUrl);

    ArgumentCaptor<Song> songCaptor = ArgumentCaptor.forClass(Song.class);
    verify(songRepository).save(songCaptor.capture());
    assertThat(songCaptor.getValue().getAudioUrl()).isEqualTo(publicUrl);
    assertThat(songCaptor.getValue().getAudioAsset()).isSameAs(mediaAssetCaptor.getValue());
  }

  @Test
  void importSongsSkipsNonAudioObjects() {
    UUID jobId = UUID.randomUUID();
    R2ObjectSummary object =
        new R2ObjectSummary(
            "imports/cover.png", 2048L, Instant.parse("2026-05-29T12:00:00Z"), "etag-2");

    when(r2StorageService.getBucketName()).thenReturn("music-streaming-audio");
    when(r2StorageService.listObjects("imports/")).thenReturn(List.of(object));
    when(r2ImportJobRepository.save(any(R2ImportJob.class)))
        .thenAnswer(
            invocation -> {
              R2ImportJob job = invocation.getArgument(0);
              if (job.getId() == null) {
                job.setId(jobId);
              }
              return job;
            });
    when(r2ImportJobItemRepository.save(any(R2ImportJobItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    R2SongImportResponse response = r2SongImportService.importSongs("imports/");

    assertThat(response.importedSongs()).isZero();
    assertThat(response.skippedObjects()).isEqualTo(1);
    assertThat(response.items()).extracting("reason").containsExactly("not an audio object");
  }
}
