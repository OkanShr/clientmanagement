package com.xera.clientmanagement.repository;

import com.xera.clientmanagement.entity.ClientPdf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientPdfRepository extends JpaRepository<ClientPdf, Long> {

    List<ClientPdf> findByClient_ClientId(Long clientId);

}
