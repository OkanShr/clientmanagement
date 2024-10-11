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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.xera.clientmanagement.utils.encryptionUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class AppointmentPdfServiceImpl implements AppointmentPdfService {

    private final AmazonS3 amazonS3;
    private final FileStore fileStore;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentPdfRepository appointmentPdfRepository;
    private final PdfFileRepository pdfFileRepository;
    private final encryptionUtil encryptionUtil;

    @Override
    public List<PdfFile> getAppointmentPdfs(Long appointmentId, Long clientId) {
        List<AppointmentPdf> appointmentPdfsMetadata = appointmentPdfRepository.findByAppointment_AppointmentId(appointmentId);
        List<PdfFile> appointmentPdfs = new ArrayList<>();

        for (AppointmentPdf appointmentPdfMetadata : appointmentPdfsMetadata) {
            PdfFile pdfFile = appointmentPdfMetadata.getPdfFile();
            encryptionUtil.decryptAllFields(pdfFile); // Decrypt the fields of the PdfFile

            String key = buildS3Key(appointmentId, clientId, pdfFile.getFileName());
            String pdfUrl = generatePresignedUrl(key);
            pdfFile.setFilePath(pdfUrl);
            appointmentPdfs.add(pdfFile);
        }

        return appointmentPdfs;
    }

    @Override
    public List<PdfFile> getAllAppointmentPdfsByClientId(Long clientId) {
        List<Appointment> appointments = appointmentRepository.findAllByClient_ClientId(clientId);
        List<PdfFile> allAppointmentPdfs = new ArrayList<>();

        for (Appointment appointment : appointments) {
            List<PdfFile> appointmentPdfs = getAppointmentPdfs(appointment.getAppointmentId(), clientId);
            allAppointmentPdfs.addAll(appointmentPdfs);
        }

        return allAppointmentPdfs;
    }

    @Override
    public void uploadAppointmentPdf(Long appointmentId, Long clientId, MultipartFile file, String type, String bearerToken) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        String fileName = file.getOriginalFilename();

        try {
            fileStore.save(clientId, fileName, Optional.empty(), file.getInputStream(), true, Optional.of(appointmentId));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store PDF: " + fileName, e);
        }

        PdfFile pdfFile = new PdfFile();
        pdfFile.setFileName(fileName);
        pdfFile.setType(type);
        pdfFile.setFilePath(clientId + "/pdfs/" + appointmentId + "/" + fileName);

        encryptionUtil.encryptAllFields(pdfFile); // Encrypt all fields of the PdfFile
        pdfFileRepository.save(pdfFile);

        AppointmentPdf appointmentPdf = new AppointmentPdf();
        appointmentPdf.setAppointment(appointment);
        appointmentPdf.setPdfFile(pdfFile);
        appointmentPdfRepository.save(appointmentPdf);
    }

    @Override
    public void deleteAppointmentPdf(Long appointmentId, Long clientId, Long pdfId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        AppointmentPdf appointmentPdf = appointmentPdfRepository.findByPdfFile_PdfId(pdfId)
                .orElseThrow(() -> new IllegalArgumentException("Pdf not found"));

        if (!appointmentPdf.getAppointment().getAppointmentId().equals(appointmentId)) {
            throw new IllegalArgumentException("Pdf does not belong to the appointment");
        }

        String key = buildS3Key(appointmentId, clientId, appointmentPdf.getPdfFile().getFileName());
        amazonS3.deleteObject(new DeleteObjectRequest("auraaesthfiles", key));

        appointmentPdfRepository.delete(appointmentPdf);
        pdfFileRepository.delete(appointmentPdf.getPdfFile());
    }

    private String generatePresignedUrl(String key) {
        Date expiration = new Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest("auraaesthfiles", key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        URL preSignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return preSignedUrl.toString();
    }

    private String buildS3Key(Long appointmentId, Long clientId, String fileName) {
        return clientId + "/pdfs/" + appointmentId + "/" + fileName;
    }
}
