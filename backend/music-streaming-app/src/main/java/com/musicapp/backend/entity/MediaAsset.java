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
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "media_assets")
public class MediaAsset {

  @Id
  @GeneratedValue
  @JdbcTypeCode(SqlTypes.UUID)
  @Column(name = "media_asset_id", nullable = false, updatable = false)
  private UUID id;

  @JdbcTypeCode(SqlTypes.UUID)
  @Column(name = "owner_user_id")
  private UUID ownerUserId;

  @JdbcTypeCode(SqlTypes.UUID)
  @Column(name = "uploaded_by_user_id")
  private UUID uploadedByUserId;

  @Column(name = "asset_type", nullable = false, length = 32)
  private String assetType;

  @Column(name = "storage_provider", nullable = false, length = 64)
  private String storageProvider;

  @Column(name = "bucket_name", length = 255)
  private String bucketName;

  @Column(name = "storage_key", length = 1024)
  private String storageKey;

  @Column(name = "public_url", nullable = false, length = 1024)
  private String publicUrl;

  @Column(name = "mime_type", length = 120)
  private String mimeType;

  @Column(name = "file_size_bytes")
  private Long fileSizeBytes;

  @Column(name = "checksum", length = 128)
  private String checksum;

  @Column(name = "etag", length = 128)
  private String etag;

  @Column(name = "duration_seconds")
  private Integer durationSeconds;

  @Column(name = "width")
  private Integer width;

  @Column(name = "height")
  private Integer height;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @Column(name = "last_modified_at")
  private Instant lastModifiedAt;

  @Column(name = "imported_at")
  private Instant importedAt;

  @Column(name = "import_source", length = 64)
  private String importSource;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
