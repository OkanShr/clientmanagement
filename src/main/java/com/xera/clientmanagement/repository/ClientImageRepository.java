package com.xera.clientmanagement.repository;

import com.xera.clientmanagement.entity.ClientImages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientImageRepository extends JpaRepository<ClientImages, Long> {
    List<ClientImages> findByClient_ClientId(Long clientId);
}