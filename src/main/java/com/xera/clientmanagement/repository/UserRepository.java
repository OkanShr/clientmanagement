package com.xera.clientmanagement.repository;

import com.xera.clientmanagement.entity.Doctor;
import com.xera.clientmanagement.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByUsername(String username);
    Doctor findByRole(Role role);
}
