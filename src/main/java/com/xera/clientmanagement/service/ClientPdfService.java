package com.xera.clientmanagement.service;

import com.xera.clientmanagement.entity.ClientPdf;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClientPdfService {
    void uploadClientPdf(Long clientId, MultipartFile file, String bearerToken);

    List<ClientPdf> getClientPdfs(Long clientId);

    void deleteClientPdf(Long clientId, Long pdfId);
}
