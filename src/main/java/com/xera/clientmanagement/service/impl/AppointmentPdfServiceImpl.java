package com.xera.clientmanagement.service.impl;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.xera.clientmanagement.Filestore.FileStore;
import com.xera.clientmanagement.entity.Appointment;
import com.xera.clientmanagement.entity.AppointmentPdf;
import com.xera.clientmanagement.entity.PdfFile;
import com.xera.clientmanagement.repository.AppointmentPdfRepository;
import com.xera.clientmanagement.repository.AppointmentRepository;
import com.xera.clientmanagement.repository.PdfFileRepository;
import com.xera.clientmanagement.service.AppointmentPdfService;
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
public class AppointmentPdfServiceImpl implements AppointmentPdfService {

    private final AmazonS3 amazonS3;
    private final FileStore fileStore;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentPdfRepository appointmentPdfRepository;
    private final PdfFileRepository pdfFileRepository;

    @Autowired
    public AppointmentPdfServiceImpl(AmazonS3 amazonS3, FileStore fileStore, AppointmentRepository appointmentRepository, AppointmentPdfRepository appointmentPdfRepository, PdfFileRepository pdfFileRepository) {
        this.amazonS3 = amazonS3;
        this.fileStore = fileStore;
        this.appointmentRepository = appointmentRepository;
        this.appointmentPdfRepository = appointmentPdfRepository;
        this.pdfFileRepository = pdfFileRepository;
    }

    @Override
    public List<PdfFile> getAppointmentPdfs(Long appointmentId) {
        // Retrieve appointment PDFs metadata from your database/repository
        List<AppointmentPdf> appointmentPdfsMetadata = appointmentPdfRepository.findByAppointment_AppointmentId(appointmentId);

        // Retrieve PDFs from Amazon S3 based on the metadata
        List<PdfFile> appointmentPdfs = new ArrayList<>();
        for (AppointmentPdf appointmentPdfMetadata : appointmentPdfsMetadata) {
            // Create a new AppointmentPdf object
            PdfFile pdfFile = appointmentPdfMetadata.getPdfFile();

            // Construct the key using appointmentId and file name
            String key = appointmentId + "/pdfs/" + pdfFile.getFileName(); // Use appointmentId in the key

            // Generate a pre-signed URL for the PDF
            String pdfUrl = generatePresignedUrl(key); // Pass the key to generatePresignedUrl
            pdfFile.setFilePath(pdfUrl); // Set the pre-signed URL as the PDF URL

            // Add the appointment PDF to the list
            appointmentPdfs.add(pdfFile);
        }

        return appointmentPdfs;
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
    public List<PdfFile> getAllAppointmentPdfsByClientId(Long clientId) {
        List<Appointment> appointments = appointmentRepository.findAllByClient_ClientId(clientId);
        List<PdfFile> allAppointmentPdfs = new ArrayList<>();

        for (Appointment appointment : appointments) {
            List<PdfFile> appointmentPdfs = getAppointmentPdfs(appointment.getAppointmentId());
            allAppointmentPdfs.addAll(appointmentPdfs);
        }

        return allAppointmentPdfs;
    }

    @Override
    public void uploadAppointmentPdf(Long appointmentId, MultipartFile file, String file_type, String bearerToken) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        String fileName = file.getOriginalFilename();

        try {
            fileStore.save(appointmentId, fileName, Optional.empty(), file.getInputStream(), bearerToken);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store PDF: " + fileName, e);
        }

        PdfFile pdfFile = new PdfFile();
        pdfFile.setFileName(fileName);
        pdfFile.setFilePath(appointmentId + "/pdfs/" + fileName);
        pdfFileRepository.save(pdfFile);

        AppointmentPdf appointmentPdf = new AppointmentPdf();
        appointmentPdf.setAppointment(appointment);
        appointmentPdf.setPdfType(file_type);
        appointmentPdf.setPdfFile(pdfFile);
        appointmentPdfRepository.save(appointmentPdf);
    }

    @Override
    public void deleteAppointmentPdf(Long appointmentId, Long pdfId) {
        // Retrieve the appointment and PDF metadata
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        AppointmentPdf appointmentPdf = appointmentPdfRepository.findByPdfFile_PdfId(pdfId)
                .orElseThrow(() -> new IllegalArgumentException("Pdf not found"));

        if (!appointmentPdf.getAppointment().getAppointmentId().equals(appointmentId)) {
            throw new IllegalArgumentException("Pdf does not belong to the appointment");
        }

        // Construct the key using appointmentId and PDF file name
        String key = appointmentId + "/pdfs/" + appointmentPdf.getPdfFile().getFileName();

        // Delete the PDF from S3
        amazonS3.deleteObject(new DeleteObjectRequest("xeramedimages", key));

        // Remove PDF metadata from the database
        appointmentPdfRepository.delete(appointmentPdf);
        pdfFileRepository.delete(appointmentPdf.getPdfFile());
    }
}
