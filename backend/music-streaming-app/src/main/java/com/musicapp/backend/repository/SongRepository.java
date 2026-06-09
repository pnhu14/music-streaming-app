package com.musicapp.backend.repository;

import com.musicapp.backend.entity.Song;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<Song, UUID> {

  @EntityGraph(attributePaths = "album.primaryArtist")
  List<Song> findAllByOrderByTitleAsc();

  @EntityGraph(attributePaths = "album.primaryArtist")
  List<Song> findByTitleContainingIgnoreCaseOrderByTitleAsc(String title);

  @EntityGraph(attributePaths = "album.primaryArtist")
  List<Song> findByDeletedAtIsNullOrderByTitleAsc();

  @EntityGraph(attributePaths = "album.primaryArtist")
  List<Song> findByDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByTitleAsc(String title);

  @Override
  @EntityGraph(attributePaths = "album.primaryArtist")
  Optional<Song> findById(UUID id);

  @EntityGraph(attributePaths = {"album.primaryArtist", "audioAsset"})
  Optional<Song> findByIdAndDeletedAtIsNull(UUID id);

  @EntityGraph(attributePaths = {"album.primaryArtist", "audioAsset"})
  Optional<Song> findBySlugAndDeletedAtIsNull(String slug);

  Optional<Song> findByAudioUrlAndDeletedAtIsNull(String audioUrl);

  Optional<Song> findByAudioAsset_IdAndDeletedAtIsNull(UUID audioAssetId);

  boolean existsBySlugAndDeletedAtIsNull(String slug);

  boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, UUID id);

  boolean existsByAudioUrlAndDeletedAtIsNull(String audioUrl);

  boolean existsByAudioAsset_IdAndDeletedAtIsNull(UUID audioAssetId);
}
