package com.musicapp.backend.repository;

import com.musicapp.backend.entity.R2ImportJobItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface R2ImportJobItemRepository extends JpaRepository<R2ImportJobItem, Long> {}
