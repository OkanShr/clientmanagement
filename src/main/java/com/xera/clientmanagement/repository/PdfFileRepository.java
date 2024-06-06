package com.xera.clientmanagement.repository;

import com.xera.clientmanagement.entity.PdfFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfFileRepository extends JpaRepository<PdfFile, Long> {
}
