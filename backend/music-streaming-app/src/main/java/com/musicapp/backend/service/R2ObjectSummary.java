package com.musicapp.backend.service;

import java.time.Instant;

public record R2ObjectSummary(String key, Long size, Instant lastModified, String eTag) {}
