package com.musicapp.backend.repository;

import com.musicapp.backend.entity.R2ImportJob;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface R2ImportJobRepository extends JpaRepository<R2ImportJob, UUID> {}
