package com.xera.clientmanagement.controller;

import com.xera.clientmanagement.dto.ClientDto;
import com.xera.clientmanagement.entity.Client;
import com.xera.clientmanagement.exception.DuplicateEmailException;
import com.xera.clientmanagement.exception.ResourceNotFoundException;
import com.xera.clientmanagement.service.ClientService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/client")
public class ClientController {
    private final ClientService clientService;


    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody ClientDto clientDto) {
        try {
            Client client = clientService.createClient(clientDto);
            return new ResponseEntity<>(client, HttpStatus.CREATED);
        } catch (DuplicateEmailException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClient(@PathVariable("id") Long clientId){
        ClientDto clientDto = clientService.getClientById(clientId);
        return ResponseEntity.ok(clientDto);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients(){
        List<ClientDto> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PutMapping("{id}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable("id") Long clientId,@RequestBody ClientDto updatedClient){
        ClientDto clientDto = clientService.updateClient(clientId,updatedClient);
        return ResponseEntity.ok(clientDto);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteClient(@PathVariable("id") Long clientId){
        clientService.deleteClient(clientId);
        return ResponseEntity.ok("Client Deleted Successfully");
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/created/last{months}months")
    public ResponseEntity<Long> getClientsCreatedInLastMonths(@PathVariable int months) {
        long count = clientService.getClientsCreatedInLastMonths(months);
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/total")
    public ResponseEntity<Long> getTotalClients() {
        long count = clientService.getTotalClients();
        return ResponseEntity.ok(count);
    }

    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/created/forEachLast6Months")
    public ResponseEntity<Map<Integer, Long>> getClientsByLast6Months() {
        Map<Integer, Long> clientsByMonth = clientService.getClientsByLast6Months();
        return ResponseEntity.ok(clientsByMonth);
    }

}
