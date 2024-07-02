package com.xera.clientmanagement.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.xera.clientmanagement.dto.ClientDto;
import com.xera.clientmanagement.entity.*;
import com.xera.clientmanagement.exception.DuplicateEmailException;
import com.xera.clientmanagement.exception.ResourceNotFoundException;
import com.xera.clientmanagement.mapper.ClientMapper;
import com.xera.clientmanagement.repository.*;
import com.xera.clientmanagement.service.ClientService;
import com.xera.clientmanagement.utils.encryptionUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ClientServiceImpl implements ClientService {

    private static final String BUCKET_NAME = "xeramedimages";
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentPdfRepository appointmentPdfRepository;
    private final PdfFileRepository pdfFileRepository;
    private final ClientPdfRepository clientPdfRepository;
    private final com.xera.clientmanagement.utils.encryptionUtil encryptionUtil;


    @Override
    public Client createClient(ClientDto clientDto) {
        Client client = new Client();
        BeanUtils.copyProperties(clientDto, client);

        Doctor doctor = userRepository.findById(clientDto.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        client.setDoctor(doctor);

        // Encrypt all fields before saving
        encryptionUtil.encryptAllFields(client);

        try {
            return clientRepository.save(client);
        } catch (DataIntegrityViolationException error) {
            throw new DuplicateEmailException("Email already taken!", error);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Something went wrong while creating the client");
        }
    }

    @Override
    public ClientDto getClientById(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client doesn't exist with the given ID: " + clientId));

        // Decrypt all fields after retrieving
        encryptionUtil.decryptAllFields(client);

        return ClientMapper.mapToClientDto(client);
    }

    @Override
    public List<ClientDto> getAllClients() {
        String username = getCurrentUsername();
        Doctor doctor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        return clientRepository.findByDoctor(doctor).stream()
                .map(client -> {
                    // Decrypt all fields for each client
                    encryptionUtil.decryptAllFields(client);
                    return ClientMapper.mapToClientDto(client);
                })
                .collect(Collectors.toList());
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null) ? authentication.getName() : null;
    }

    @Override
    public ClientDto updateClient(Long clientId, ClientDto updatedClient) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found!"));

        BeanUtils.copyProperties(updatedClient, client, "id", "doctor");

        // Encrypt all fields before saving
        encryptionUtil.encryptAllFields(client);

        Client updatedClientObj = clientRepository.save(client);

        return ClientMapper.mapToClientDto(updatedClientObj);
    }

    @Override
    public void deleteClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found!"));

        // Delete all ClientPdf objects
        List<ClientPdf> clientPdfs = clientPdfRepository.findByClient_ClientId(clientId);
        for (ClientPdf clientPdf : clientPdfs) {
            String clientPdfKey = generateS3Key(clientId, clientPdf.getPdfFile().getFileName());
            amazonS3.deleteObject(new DeleteObjectRequest(BUCKET_NAME, clientPdfKey));
            clientPdfRepository.delete(clientPdf);
            pdfFileRepository.delete(clientPdf.getPdfFile());
        }

        // Retrieve all Appointments associated with the client
        List<Appointment> appointments = appointmentRepository.findAllByClient_ClientId(clientId);

        // Delete all associated AppointmentPdf objects and Appointments
        for (Appointment appointment : appointments) {
            List<AppointmentPdf> appointmentPdfs = appointmentPdfRepository.findByAppointment_AppointmentId(appointment.getAppointmentId());
            for (AppointmentPdf appointmentPdf : appointmentPdfs) {
                String appointmentPdfKey = buildS3Key(appointment.getAppointmentId(), clientId, appointmentPdf.getPdfFile().getFileName());
                amazonS3.deleteObject(new DeleteObjectRequest(BUCKET_NAME, appointmentPdfKey));
                appointmentPdfRepository.delete(appointmentPdf);
                pdfFileRepository.delete(appointmentPdf.getPdfFile());
            }
            appointmentRepository.delete(appointment);
        }

        // Finally, delete the client
        clientRepository.deleteById(clientId);
    }

    @Override
    public long getClientsCreatedInLastMonths(int months) {
        LocalDateTime date = LocalDateTime.now().minusMonths(months);
        return clientRepository.countClientsCreatedSince(date);
    }

    @Override
    public long getTotalClients() {
        return clientRepository.count();
    }

    @Override
    public Map<Integer, Long> getClientsByLast6Months() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(6)
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Object[]> results = clientRepository.countClientsByMonth(startDate);

        Map<Integer, Long> clientsByMonth = results.stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> (Long) result[1]
                ));

        for (int i = 0; i < 6; i++) {
            int month = LocalDateTime.now().minusMonths(i).getMonthValue();
            clientsByMonth.putIfAbsent(month, 0L);
        }

        return clientsByMonth;
    }

    private String generateS3Key(Long clientId, String fileName) {
        return clientId + "/pdfs/" + fileName;
    }

    private String buildS3Key(Long appointmentId, Long clientId, String fileName) {
        return clientId + "/pdfs/" + appointmentId + "/" + fileName;
    }
}
