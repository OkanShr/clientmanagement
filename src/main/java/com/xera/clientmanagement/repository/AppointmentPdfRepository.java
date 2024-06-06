package com.xera.clientmanagement.repository;

import com.xera.clientmanagement.entity.AppointmentPdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentPdfRepository extends JpaRepository<AppointmentPdf, Long> {
    List<AppointmentPdf> findByAppointment_AppointmentId(Long appointmentId);
}
