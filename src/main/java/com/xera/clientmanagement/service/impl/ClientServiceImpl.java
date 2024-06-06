package com.xera.clientmanagement.service.impl;

import com.xera.clientmanagement.dto.ClientDto;
import com.xera.clientmanagement.entity.Client;
import com.xera.clientmanagement.entity.Doctor;
import com.xera.clientmanagement.exception.ResourceNotFoundException;
import com.xera.clientmanagement.mapper.ClientMapper;
import com.xera.clientmanagement.repository.ClientRepository;
import com.xera.clientmanagement.repository.UserRepository;
import com.xera.clientmanagement.service.ClientService;
import com.xera.clientmanagement.service.JwtService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ClientServiceImpl implements ClientService {
    private ClientRepository clientRepository;
    private UserRepository userRepository;
    private JwtService jwtService;




    @Override
    public Client createClient(ClientDto clientDto) {
        try{
            Client client = new Client();
            BeanUtils.copyProperties(clientDto, client);

            Doctor doctor = userRepository.findById(clientDto.getDoctorId())
                    .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

            client.setDoctor(doctor);
            return clientRepository.save(client);
        }catch (EntityNotFoundException e){
            throw new ResourceNotFoundException("Doctor not found");
        }

    }

    @Override
    public ClientDto getClientById(Long clientId) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new ResourceNotFoundException("Client doesn't exist with given id : " + clientId));
        return ClientMapper.mapToClientDto(client);
    }

    @Override
    public List<ClientDto> getAllClients() {
        // Get the authentication object from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : null;

        // Use the username to find the corresponding doctor
        Doctor doctor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // Fetch only the clients of the logged-in doctor
        List<Client> clients = clientRepository.findByDoctor(doctor);
        return clients.stream().map(ClientMapper::mapToClientDto).collect(Collectors.toList());
    }

    @Override
    public ClientDto updateClient(Long clientId, ClientDto updatedClient) {
        Client client = clientRepository.findById(clientId).orElseThrow(
                () -> new ResourceNotFoundException("Client Not Found!")
        );

        client.setFirstName(updatedClient.getFirstName());
        client.setLastName(updatedClient.getLastName());
        client.setEmail(updatedClient.getEmail());
        client.setGender(updatedClient.getGender());
        client.setPhoneNumber(updatedClient.getPhoneNumber());

        Client updatedClientObj = clientRepository.save(client);

        return ClientMapper.mapToClientDto(updatedClientObj);
    }

    @Override
    public void deleteClient(Long clientId) {
        Client client = clientRepository.findById(clientId).orElseThrow(
                () -> new ResourceNotFoundException("Client Not Found!")
        );

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
        LocalDateTime startDate = LocalDateTime.now().minusMonths(6).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Object[]> results = clientRepository.countClientsByMonth(startDate);

        Map<Integer, Long> clientsByMonth = new HashMap<>();
        for (Object[] result : results) {
            Integer month = (Integer) result[0];
            Long count = (Long) result[1];
            clientsByMonth.put(month, count);
        }

        // Ensure all 6 months are included even if they have 0 clients
        for (int i = 0; i < 6; i++) {
            int month = LocalDateTime.now().minusMonths(i).getMonthValue();
            clientsByMonth.putIfAbsent(month, 0L);
        }

        return clientsByMonth;
    }



}
