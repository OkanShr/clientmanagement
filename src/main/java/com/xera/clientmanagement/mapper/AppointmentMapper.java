package com.xera.clientmanagement.mapper;

import com.xera.clientmanagement.dto.AppointmentDto;
import com.xera.clientmanagement.entity.Appointment;

public class AppointmentMapper {
    public static AppointmentDto mapToAppointmentDto(Appointment appointment){
        return new AppointmentDto(
                appointment.getAppointmentId(),
                appointment.getActivity(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getClient().getClientId(),
                appointment.getType()
        );
    }
}
