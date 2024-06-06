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
		Doctor adminAccount = userRepository.findByRole(Role.ADMIN);
		if(null == adminAccount){
			Doctor doctor = new Doctor();

			doctor.setEmail("okan.shr@hotmail.com");
			doctor.setUsername("okan");
			doctor.setPassword(new BCryptPasswordEncoder().encode("user"));
			doctor.setRole(Role.ADMIN);
			userRepository.save(doctor);
		}
	}
}
