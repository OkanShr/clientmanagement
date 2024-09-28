package com.xera.clientmanagement.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.xera.clientmanagement.dto.AppointmentDto;
import com.xera.clientmanagement.entity.*;
import com.xera.clientmanagement.exception.ResourceNotFoundException;
import com.xera.clientmanagement.mapper.AppointmentMapper;
import com.xera.clientmanagement.repository.*;
import com.xera.clientmanagement.service.AppointmentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentPdfRepository appointmentPdfRepository;
    private final PdfFileRepository pdfFileRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;
    private final com.xera.clientmanagement.utils.encryptionUtil encryptionUtil;

    @Override
    @Transactional
    public Appointment createAppointment(AppointmentDto appointmentDto) {
        try {
            Appointment appointment = new Appointment();
            BeanUtils.copyProperties(appointmentDto, appointment);

            Client client = clientRepository.findById(appointmentDto.getClientId())
                    .orElseThrow(() -> new EntityNotFoundException("Client Not Found"));

            appointment.setClient(client);

            // Encrypt all fields of the appointment before saving
            encryptionUtil.encryptAllFields(appointment);

            return appointmentRepository.save(appointment);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Could not create appointment");
        }
    }



    @Override
    public List<AppointmentDto> getAllAppointmentsForLoggedInDoctor() {
        // Get the authentication object from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : null;

        // Use the username to find the corresponding doctor
        Doctor doctor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // Fetch only the clients of the logged-in doctor
        List<Client> clients = clientRepository.findByDoctor(doctor);

        // Fetch appointments for each client and collect them all
        List<AppointmentDto> allAppointments = new ArrayList<>();
        for (Client client : clients) {
            List<Appointment> appointments = appointmentRepository.findAllByClient_ClientId(client.getClientId());
            allAppointments.addAll(appointments.stream()
                    .map(appointment -> {
                        encryptionUtil.decryptAllFields(appointment);
                        return AppointmentMapper.mapToAppointmentDto(appointment);
                    })
                    .toList());
        }
        return allAppointments;
    }


    @Override
    public AppointmentDto getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment Not Found"));

        // Decrypt all fields after retrieving
        encryptionUtil.decryptAllFields(appointment);

        return AppointmentMapper.mapToAppointmentDto(appointment);
    }

    @Override
    public List<AppointmentDto> getAppointmentsByClientId(Long clientId) {
        List<Appointment> appointments = appointmentRepository.findAllByClient_ClientId(clientId);
        return appointments.stream()
                .map(appointment -> {
                    // Decrypt all fields for each appointment
                    encryptionUtil.decryptAllFields(appointment);
                    return AppointmentMapper.mapToAppointmentDto(appointment);
                })
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentDto updateAppointment(Long appointmentId, AppointmentDto updatedAppointment) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment Not Found!"));

        appointment.setActivity(updatedAppointment.getActivity());
        appointment.setDate(updatedAppointment.getDate());

        // Encrypt all fields before saving
        encryptionUtil.encryptAllFields(appointment);

        Appointment updatedAppointmentObj = appointmentRepository.save(appointment);

        return AppointmentMapper.mapToAppointmentDto(updatedAppointmentObj);
    }

    @Override
    public void deleteAppointment(Long appointmentId, Long clientId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment Not Found!"));

        // Retrieve all appointmentPdfs associated with the appointment
        List<AppointmentPdf> appointmentPdfs = appointmentPdfRepository.findByAppointment_AppointmentId(appointmentId);

        // Delete all associated PDFs from S3 and repositories
        for (AppointmentPdf appointmentPdf : appointmentPdfs) {
            String key = buildS3Key(appointmentId, clientId, appointmentPdf.getPdfFile().getFileName());
            amazonS3.deleteObject(new DeleteObjectRequest("xeramedimages", key));
            appointmentPdfRepository.delete(appointmentPdf);
            pdfFileRepository.delete(appointmentPdf.getPdfFile());
        }

        // Finally, delete the appointment
        appointmentRepository.deleteById(appointmentId);
    }

    @Override
    public Map<String, Long> getAppointmentsCountByType() {
        List<String> types = List.of("Operation", "Beratung", "Injektion", "Laser");
        Map<String, Long> appointmentsCountByType = new HashMap<>();
        for (String type : types) {
            long count = appointmentRepository.findAllByType(type).size();
            appointmentsCountByType.put(type, count);
        }
        return appointmentsCountByType;
    }

    private String buildS3Key(Long appointmentId, Long clientId, String fileName) {
        return clientId + "/pdfs/" + appointmentId + "/" + fileName;
    }



}
