package com.xera.clientmanagement.repository;

import com.xera.clientmanagement.entity.AppointmentPdf;
import com.xera.clientmanagement.entity.ClientPdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentPdfRepository extends JpaRepository<AppointmentPdf, Long> {
    List<AppointmentPdf> findByAppointment_AppointmentId(Long appointmentId);

    Optional<AppointmentPdf> findByPdfFile_PdfId(Long pdfId);

}
