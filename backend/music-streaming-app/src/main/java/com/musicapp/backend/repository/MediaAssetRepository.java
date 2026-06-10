package com.musicapp.backend.repository;

import com.musicapp.backend.entity.MediaAsset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {

  Optional<MediaAsset> findByStorageProviderAndBucketNameAndStorageKeyAndAssetTypeAndStatusNot(
      String storageProvider,
      String bucketName,
      String storageKey,
      String assetType,
      String status);
}
