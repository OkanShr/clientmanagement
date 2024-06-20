package com.xera.clientmanagement.mapper;

import com.xera.clientmanagement.dto.ClientDto;
import com.xera.clientmanagement.entity.Client;

public class ClientMapper {
    public static ClientDto mapToClientDto(Client client){
        return new ClientDto(
                client.getClientId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getGender(),
                client.getBirthDate(),
                client.getPhoneNumber(),
                client.getDoctor().getUserId()

        );
    }

}
