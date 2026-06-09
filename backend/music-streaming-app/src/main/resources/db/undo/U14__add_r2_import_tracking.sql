BEGIN;

DROP INDEX IF EXISTS idx_r2_import_job_items_media_asset_id;
DROP INDEX IF EXISTS idx_r2_import_job_items_job_status;
DROP INDEX IF EXISTS idx_r2_import_jobs_started_at;
DROP INDEX IF EXISTS uk_r2_import_job_items_job_object;

DROP TABLE IF EXISTS r2_import_job_items;
DROP TABLE IF EXISTS r2_import_jobs;

DROP INDEX IF EXISTS idx_media_assets_storage_lookup;
DROP INDEX IF EXISTS uk_media_assets_storage_object;

ALTER TABLE media_assets DROP COLUMN IF EXISTS import_source;
ALTER TABLE media_assets DROP COLUMN IF EXISTS imported_at;
ALTER TABLE media_assets DROP COLUMN IF EXISTS last_modified_at;
ALTER TABLE media_assets DROP COLUMN IF EXISTS etag;
ALTER TABLE media_assets DROP COLUMN IF EXISTS bucket_name;

ALTER TABLE media_assets ALTER COLUMN storage_key TYPE VARCHAR(512);
ALTER TABLE songs ALTER COLUMN audio_url TYPE VARCHAR(512);

COMMIT;
