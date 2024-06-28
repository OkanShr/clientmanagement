package com.xera.clientmanagement.service.impl;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.xera.clientmanagement.Filestore.FileStore;
import com.xera.clientmanagement.entity.Client;
import com.xera.clientmanagement.entity.ClientPdf;
import com.xera.clientmanagement.entity.PdfFile;
import com.xera.clientmanagement.repository.ClientPdfRepository;
import com.xera.clientmanagement.repository.ClientRepository;
import com.xera.clientmanagement.repository.PdfFileRepository;
import com.xera.clientmanagement.service.ClientPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientPdfServiceImpl implements ClientPdfService {

    private static final String BUCKET_NAME = "xeramedimages";
    private static final long URL_EXPIRATION_TIME = 3600000; // 1 hour

    private final AmazonS3 amazonS3;
    private final FileStore fileStore;
    private final ClientRepository clientRepository;
    private final ClientPdfRepository clientPdfRepository;
    private final PdfFileRepository pdfFileRepository;

    @Autowired
    public ClientPdfServiceImpl(AmazonS3 amazonS3, FileStore fileStore, ClientRepository clientRepository,
                                ClientPdfRepository clientPdfRepository, PdfFileRepository pdfFileRepository) {
        this.amazonS3 = amazonS3;
        this.fileStore = fileStore;
        this.clientRepository = clientRepository;
        this.clientPdfRepository = clientPdfRepository;
        this.pdfFileRepository = pdfFileRepository;
    }

    @Override
    public List<PdfFile> getClientPdfs(Long clientId) {
        return clientPdfRepository.findByClient_ClientId(clientId).stream()
                .map(this::mapToPdfFileWithPresignedUrl)
                .collect(Collectors.toList());
    }

    private PdfFile mapToPdfFileWithPresignedUrl(ClientPdf clientPdf) {
        PdfFile pdfFile = clientPdf.getPdfFile();
        String key = generateS3Key(clientPdf.getClient().getClientId(), pdfFile.getFileName());
        pdfFile.setFilePath(generatePresignedUrl(key));
        return pdfFile;
    }

    private String generateS3Key(Long clientId, String fileName) {
        return clientId + "/pdfs/" + fileName;
    }

    private String generatePresignedUrl(String key) {
        Date expiration = new Date(System.currentTimeMillis() + URL_EXPIRATION_TIME);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(BUCKET_NAME, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        URL url = amazonS3.generatePresignedUrl(request);
        return url.toString();
    }

    @Override
    public void uploadClientPdf(Long clientId, MultipartFile file, String type, String bearerToken) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        String fileName = file.getOriginalFilename();

        try {
            fileStore.save(clientId, fileName, Optional.empty(), file.getInputStream(),
                    false, Optional.empty());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store PDF: " + fileName, e);
        }

        PdfFile pdfFile = new PdfFile();
        pdfFile.setFileName(fileName);
        pdfFile.setType(type);
        pdfFile.setFilePath(generateS3Key(clientId, fileName));
        pdfFileRepository.save(pdfFile);

        ClientPdf clientPdf = new ClientPdf();
        clientPdf.setClient(client);
        clientPdf.setPdfFile(pdfFile);
        clientPdfRepository.save(clientPdf);
    }

    @Override
    public void deleteClientPdf(Long clientId, Long pdfId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        ClientPdf clientPdf = clientPdfRepository.findByPdfFile_PdfId(pdfId)
                .orElseThrow(() -> new IllegalArgumentException("Pdf not found"));

        if (!clientPdf.getClient().getClientId().equals(clientId)) {
            throw new IllegalArgumentException("Pdf does not belong to the client");
        }

        String key = generateS3Key(clientId, clientPdf.getPdfFile().getFileName());
        amazonS3.deleteObject(new DeleteObjectRequest(BUCKET_NAME, key));

        clientPdfRepository.delete(clientPdf);
        pdfFileRepository.delete(clientPdf.getPdfFile());
    }
}
