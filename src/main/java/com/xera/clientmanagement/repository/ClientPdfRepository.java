package com.xera.clientmanagement.repository;

import com.xera.clientmanagement.entity.ClientPdf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientPdfRepository extends JpaRepository<ClientPdf, Long> {

    List<ClientPdf> findByClient_ClientId(Long clientId);

    Optional<ClientPdf> findByPdfFile_PdfId(Long pdfId);

}
