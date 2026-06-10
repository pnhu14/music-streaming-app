package com.musicapp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "r2_import_job_items")
public class R2ImportJobItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "r2_import_job_item_id", nullable = false, updatable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "r2_import_job_id", nullable = false)
  private R2ImportJob job;

  @Column(name = "bucket_name", nullable = false, length = 255)
  private String bucketName;

  @Column(name = "object_key", nullable = false, length = 1024)
  private String objectKey;

  @Column(name = "object_size_bytes")
  private Long objectSizeBytes;

  @Column(name = "object_last_modified_at")
  private Instant objectLastModifiedAt;

  @Column(name = "etag", length = 128)
  private String etag;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @Column(name = "reason", length = 255)
  private String reason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "song_id")
  private Song song;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "media_asset_id")
  private MediaAsset mediaAsset;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
