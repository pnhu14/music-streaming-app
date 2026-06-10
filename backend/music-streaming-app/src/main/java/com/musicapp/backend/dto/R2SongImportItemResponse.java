package com.musicapp.backend.dto;

import java.util.UUID;

public record R2SongImportItemResponse(
    String objectKey, String status, String reason, UUID songId, UUID mediaAssetId) {}
