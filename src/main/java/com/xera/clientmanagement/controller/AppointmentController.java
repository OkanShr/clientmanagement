package com.xera.clientmanagement.controller;

import com.xera.clientmanagement.dto.AppointmentDto;
import com.xera.clientmanagement.entity.Appointment;
import com.xera.clientmanagement.service.AppointmentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {
    private  final AppointmentService appointmentService;
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody AppointmentDto appointmentDto) {
        Appointment appointment = appointmentService.createAppointment(appointmentDto);
        return new ResponseEntity<>(appointment, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDto> getAppointment(@PathVariable("id") Long appointmentId){
        AppointmentDto appointmentDto = appointmentService.getAppointmentById(appointmentId);
        return  ResponseEntity.ok(appointmentDto);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("client/{clientId}")
    public  ResponseEntity<List<AppointmentDto>> getAllAppointments(@PathVariable Long clientId){
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByClientId(clientId);
        return ResponseEntity.ok(appointments);
    }
    @GetMapping("clients/all")
    public  ResponseEntity<List<AppointmentDto>> getAllAppointmentsByDoctor(){
        List<AppointmentDto> appointments = appointmentService.getAllAppointmentsForLoggedInDoctor();
        return ResponseEntity.ok(appointments);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PutMapping("{id}")
    public ResponseEntity<AppointmentDto> updateAppointment(@PathVariable("id") Long appointmentId,@RequestBody AppointmentDto updatedAppointment){
        AppointmentDto appointmentDto = appointmentService.updateAppointment(appointmentId,updatedAppointment);
        return ResponseEntity.ok(appointmentDto);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteAppointment(@PathVariable("id") Long appointmentId){
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.ok("Appointment Deleted Successfully");
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/countByType")
    public ResponseEntity<Map<String, Long>> getAppointmentsCountByType() {
        Map<String, Long> appointmentsCountByType = appointmentService.getAppointmentsCountByType();
        return new ResponseEntity<>(appointmentsCountByType, HttpStatus.OK);
    }


}
