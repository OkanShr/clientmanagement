package com.xera.clientmanagement.repository;

import com.xera.clientmanagement.entity.Client;
import com.xera.clientmanagement.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByDoctor(Doctor doctor);
    @Query("SELECT COUNT(c) FROM Client c WHERE c.createdAt >= :date")
    long countClientsCreatedSince(@Param("date") LocalDateTime date);

    @Query("SELECT MONTH(c.createdAt) as month, COUNT(c) as count FROM Client c WHERE c.createdAt >= :startDate GROUP BY MONTH(c.createdAt)")
    List<Object[]> countClientsByMonth(@Param("startDate") LocalDateTime startDate);

    long count();
}