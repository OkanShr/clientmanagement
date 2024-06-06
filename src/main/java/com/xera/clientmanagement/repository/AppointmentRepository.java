package com.xera.clientmanagement.repository;

import com.xera.clientmanagement.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    List<Appointment> findAllByClient_ClientId(Long clientId);

    List<Appointment> findAllByType(String type);

}
