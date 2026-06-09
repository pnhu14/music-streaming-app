package com.musicapp.backend.dto;

import java.util.List;
import java.util.UUID;

public record R2SongImportResponse(
    UUID jobId,
    String status,
    int scannedObjects,
    int importedSongs,
    int skippedObjects,
    int failedObjects,
    List<SongDto> songs,
    List<R2SongImportItemResponse> items) {}
