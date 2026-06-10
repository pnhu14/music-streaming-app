package com.musicapp.backend.controller;

import com.musicapp.backend.dto.R2SongImportResponse;
import com.musicapp.backend.service.R2SongImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/songs")
public class AdminSongController {

  private final R2SongImportService r2SongImportService;

  @PostMapping("/import-r2")
  @PreAuthorize("hasRole('ADMIN')")
  public R2SongImportResponse importR2Songs(@RequestParam(required = false) String prefix) {
    return r2SongImportService.importSongs(prefix);
  }
}
