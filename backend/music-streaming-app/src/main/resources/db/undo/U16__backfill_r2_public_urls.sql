BEGIN;

UPDATE songs song
SET audio_url = media_asset.storage_key
FROM media_assets media_asset
WHERE song.audio_asset_id = media_asset.media_asset_id
  AND media_asset.storage_provider = 'CLOUDFLARE_R2'
  AND media_asset.import_source = 'R2_IMPORT'
  AND media_asset.storage_key IS NOT NULL
  AND song.deleted_at IS NULL;

UPDATE media_assets
SET public_url = 'r2://' || bucket_name || '/' || storage_key
WHERE storage_provider = 'CLOUDFLARE_R2'
  AND import_source = 'R2_IMPORT'
  AND bucket_name IS NOT NULL
  AND storage_key IS NOT NULL;

COMMIT;
