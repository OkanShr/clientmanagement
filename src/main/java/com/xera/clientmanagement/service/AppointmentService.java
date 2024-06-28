package com.xera.clientmanagement.service;

import com.xera.clientmanagement.dto.AppointmentDto;
import com.xera.clientmanagement.entity.Appointment;

import java.util.List;
import java.util.Map;

public interface AppointmentService {
    Appointment createAppointment(AppointmentDto appointmentDto);

    List<AppointmentDto> getAllAppointmentsForLoggedInDoctor();

    AppointmentDto getAppointmentById(Long appointmentId);

    List<AppointmentDto> getAppointmentsByClientId(Long clientId);

    AppointmentDto updateAppointment(Long appointmentId, AppointmentDto updatedAppointment);

    void deleteAppointment(Long appointmentId, Long clientId);

    Map<String, Long> getAppointmentsCountByType();

}
