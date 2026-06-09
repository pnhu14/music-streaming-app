DO $$
DECLARE
    public_base_url TEXT := btrim('${r2PublicBaseUrl}');
BEGIN
    IF public_base_url = ''
        AND EXISTS (
            SELECT 1
            FROM media_assets media_asset
            JOIN songs song ON song.audio_asset_id = media_asset.media_asset_id
            WHERE media_asset.storage_provider = 'CLOUDFLARE_R2'
              AND media_asset.import_source = 'R2_IMPORT'
              AND media_asset.storage_key IS NOT NULL
              AND song.deleted_at IS NULL
              AND (
                  song.audio_url = media_asset.storage_key
                  OR media_asset.public_url LIKE 'r2://%'
              )
        )
    THEN
        RAISE EXCEPTION 'R2_PUBLIC_BASE_URL is required to backfill imported R2 public URLs';
    END IF;

    IF public_base_url <> '' THEN
        public_base_url := regexp_replace(public_base_url, '/+$', '');

        UPDATE media_assets media_asset
        SET public_url = public_base_url
            || '/'
            || replace(
                replace(
                    replace(
                        replace(media_asset.storage_key, ' ', '%20'),
                        '#',
                        '%23'
                    ),
                    '?',
                    '%3F'
                ),
                '+',
                '%2B'
            )
        WHERE media_asset.storage_provider = 'CLOUDFLARE_R2'
          AND media_asset.import_source = 'R2_IMPORT'
          AND media_asset.storage_key IS NOT NULL;

        UPDATE songs song
        SET audio_url = media_asset.public_url
        FROM media_assets media_asset
        WHERE song.audio_asset_id = media_asset.media_asset_id
          AND media_asset.storage_provider = 'CLOUDFLARE_R2'
          AND media_asset.import_source = 'R2_IMPORT'
          AND media_asset.storage_key IS NOT NULL
          AND song.deleted_at IS NULL
          AND (
              song.audio_url = media_asset.storage_key
              OR song.audio_url LIKE 'r2://%'
          );
    END IF;
END $$;
