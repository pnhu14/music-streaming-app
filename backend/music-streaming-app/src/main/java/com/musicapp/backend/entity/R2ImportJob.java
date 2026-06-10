package com.musicapp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "r2_import_jobs")
public class R2ImportJob {

  @Id
  @GeneratedValue
  @JdbcTypeCode(SqlTypes.UUID)
  @Column(name = "r2_import_job_id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "bucket_name", nullable = false, length = 255)
  private String bucketName;

  @Column(name = "prefix", length = 512)
  private String prefix;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @Column(name = "scanned_objects", nullable = false)
  private int scannedObjects;

  @Column(name = "imported_songs", nullable = false)
  private int importedSongs;

  @Column(name = "skipped_objects", nullable = false)
  private int skippedObjects;

  @Column(name = "failed_objects", nullable = false)
  private int failedObjects;

  @CreationTimestamp
  @Column(name = "started_at", nullable = false, updatable = false)
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Column(name = "error_message")
  private String errorMessage;
}
