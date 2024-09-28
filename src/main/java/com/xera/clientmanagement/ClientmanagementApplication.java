package com.xera.clientmanagement;

import com.xera.clientmanagement.entity.Role;
import com.xera.clientmanagement.entity.Doctor;
import com.xera.clientmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ClientmanagementApplication implements CommandLineRunner {
	@Autowired
	private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(ClientmanagementApplication.class, args);
	}


	public void run(String... args){
		Doctor userAccount = userRepository.findByRole(Role.ADMIN);
		if(null == userAccount){
			Doctor doctor = new Doctor();

			doctor.setEmail("admin2");
			doctor.setUsername("admin2");
			doctor.setPassword(new BCryptPasswordEncoder().encode("admin2"));
			doctor.setRole(Role.ADMIN);
			userRepository.save(doctor);
		}
	}
}
