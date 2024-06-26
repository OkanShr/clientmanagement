package com.xera.clientmanagement.service;

import com.xera.clientmanagement.entity.AppointmentPdf;
import com.xera.clientmanagement.entity.PdfFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AppointmentPdfService {
    List<PdfFile> getAllAppointmentPdfsByClientId(Long clientId);

    void uploadAppointmentPdf(Long appointmentId, Long clientId, MultipartFile file, String file_type, String bearerToken);

    List<PdfFile> getAppointmentPdfs(Long appointmentId, Long clientId);

    void deleteAppointmentPdf(Long appointmentId, Long clientId, Long pdfId);
}
