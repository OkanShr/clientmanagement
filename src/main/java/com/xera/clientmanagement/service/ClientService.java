package com.xera.clientmanagement.service;

import com.xera.clientmanagement.dto.ClientDto;
import com.xera.clientmanagement.entity.Client;

import java.util.List;
import java.util.Map;

public interface ClientService {
    Client createClient(ClientDto clientDto);

    ClientDto getClientById(Long clientId);

    List<ClientDto> getAllClients();

    ClientDto updateClient(Long clientId, ClientDto updatedClient);

    void deleteClient(Long clientId);


    long getClientsCreatedInLastMonths(int months);

    long getTotalClients();

    Map<Integer, Long> getClientsByLast6Months();
}
