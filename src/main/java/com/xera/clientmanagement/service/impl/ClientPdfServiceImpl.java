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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ClientPdfServiceImpl implements ClientPdfService {

    private final AmazonS3 amazonS3;
    private final FileStore fileStore;
    private final ClientRepository clientRepository;
    private final ClientPdfRepository clientPdfRepository;
    private final PdfFileRepository pdfFileRepository;

    @Autowired
    public ClientPdfServiceImpl(AmazonS3 amazonS3, FileStore fileStore, ClientRepository clientRepository, ClientPdfRepository clientPdfRepository, PdfFileRepository pdfFileRepository) {
        this.amazonS3 = amazonS3;
        this.fileStore = fileStore;
        this.clientRepository = clientRepository;
        this.clientPdfRepository = clientPdfRepository;
        this.pdfFileRepository = pdfFileRepository;
    }

    @Override
    public List<PdfFile> getClientPdfs(Long clientId) {
        // Retrieve client PDFs metadata from your database/repository
        List<ClientPdf> clientPdfsMetadata = clientPdfRepository.findByClient_ClientId(clientId);

        // Retrieve PDFs from Amazon S3 based on the metadata
        List<PdfFile> pdfFiles = new ArrayList<>();
        for (ClientPdf clientPdfMetadata : clientPdfsMetadata) {
            // Get the PdfFile object
            PdfFile pdfFile = clientPdfMetadata.getPdfFile();

            // Construct the key using clientId and file name
            String key = clientId + "/pdfs/" + pdfFile.getFileName();

            // Generate a pre-signed URL for the PDF
            String pdfUrl = generatePresignedUrl(key);
            pdfFile.setFilePath(pdfUrl); // Set the pre-signed URL as the PDF URL

            // Add the PdfFile to the list
            pdfFiles.add(pdfFile);
        }

        return pdfFiles;
    }

    private String generatePresignedUrl(String key) {
        // Generate a pre-signed URL for the image
        String bucketName = "xeramedimages"; // Bucket name
        Date expiration = new Date(System.currentTimeMillis() + 3600000); // Expiry time: 1 hour from now

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        // Generate the pre-signed URL
        URL preSignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        return preSignedUrl.toString();
    }


    @Override
    public void uploadClientPdf(Long clientId, MultipartFile file, String type, String bearerToken) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        String fileName = file.getOriginalFilename();

        try {
            fileStore.save(clientId, fileName, Optional.empty(), file.getInputStream(), bearerToken);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store PDF: " + fileName, e);
        }

        PdfFile pdfFile = new PdfFile();
        pdfFile.setFileName(fileName);
        pdfFile.setType(type);
        pdfFile.setFilePath(clientId + "/pdfs/" + fileName);
        pdfFileRepository.save(pdfFile);

        ClientPdf clientPdf = new ClientPdf();
        clientPdf.setClient(client);
        clientPdf.setPdfFile(pdfFile);
        clientPdfRepository.save(clientPdf);
    }

    @Override
    public void deleteClientPdf(Long clientId, Long pdfId) {
        // Retrieve the client and PDF metadata
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        ClientPdf clientPdf = clientPdfRepository.findById(pdfId)
                .orElseThrow(() -> new IllegalArgumentException("Pdf not found"));

        if (!clientPdf.getClient().getClientId().equals(clientId)) {
            throw new IllegalArgumentException("Pdf does not belong to the client");
        }

        // Construct the key using clientId and PDF file name
        String key = clientId + "/pdfs/" + clientPdf.getPdfFile().getFileName();

        // Delete the PDF from S3
        amazonS3.deleteObject(new DeleteObjectRequest("xeramedimages", key));

        // Remove PDF metadata from the database
        clientPdfRepository.delete(clientPdf);
        pdfFileRepository.delete(clientPdf.getPdfFile());
    }
}
