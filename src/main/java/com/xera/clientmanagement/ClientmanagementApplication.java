package com.xera.clientmanagement;

import com.xera.clientmanagement.entity.Role;
import com.xera.clientmanagement.entity.Doctor;
import com.xera.clientmanagement.repository.UserRepository;
import com.xera.clientmanagement.dto.SignUpRequest;
import com.xera.clientmanagement.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClientmanagementApplication implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthenticationService authenticationService;

	public static void main(String[] args) {
		SpringApplication.run(ClientmanagementApplication.class, args);
	}

	@Override
	public void run(String... args) {
//		Doctor userAccount = userRepository.findByRole(Role.ADMIN);
//		if (userAccount == null) {
//			SignUpRequest signUpRequest = new SignUpRequest();
//			signUpRequest.setEmail("admin@example.com");
//			signUpRequest.setUsername("admin");
//			signUpRequest.setPassword("admin");
//
//			authenticationService.signup(signUpRequest);
//		}
	}
}
