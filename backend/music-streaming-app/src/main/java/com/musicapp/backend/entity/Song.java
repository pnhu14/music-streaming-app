package com.musicapp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "songs")
public class Song {

  @Id
  @GeneratedValue
  @JdbcTypeCode(SqlTypes.UUID)
  @Column(name = "song_id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "slug", nullable = false, length = 200, unique = true)
  private String slug;

  @Column(name = "duration_seconds", nullable = false)
  private Integer durationSeconds;

  @Column(name = "release_date")
  private LocalDate releaseDate;

  @Column(name = "audio_url", nullable = false, length = 1024)
  private String audioUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "audio_asset_id")
  private MediaAsset audioAsset;

  @Column(name = "cover_url", length = 512)
  private String coverUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cover_asset_id")
  private MediaAsset coverAsset;

  @Column(name = "lyrics")
  private String lyrics;

  @Column(name = "explicit", nullable = false)
  private boolean explicit;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "album_id")
  private Album album;

  @Column(name = "track_number")
  private Integer trackNumber;

  @Column(name = "disc_number")
  private Integer discNumber;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
