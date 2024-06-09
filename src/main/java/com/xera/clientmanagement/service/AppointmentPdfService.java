package com.xera.clientmanagement.service;

import com.xera.clientmanagement.entity.AppointmentPdf;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AppointmentPdfService {
    void uploadAppointmentPdf(Long appointmentId, MultipartFile file, String file_type, String bearerToken);

    List<AppointmentPdf> getAppointmentPdfs(Long appointmentId);

    void deleteAppointmentPdf(Long appointmentId, Long pdfId);
}
