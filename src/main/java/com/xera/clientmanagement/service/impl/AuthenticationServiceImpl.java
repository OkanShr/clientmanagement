package com.xera.clientmanagement.service.impl;

import com.xera.clientmanagement.dto.JwtAuthenticationResponse;
import com.xera.clientmanagement.dto.RefreshTokenRequest;
import com.xera.clientmanagement.dto.SignInRequest;
import com.xera.clientmanagement.dto.SignUpRequest;
import com.xera.clientmanagement.entity.Doctor;
import com.xera.clientmanagement.entity.Role;
import com.xera.clientmanagement.exception.InvalidLoginException;
import com.xera.clientmanagement.repository.UserRepository;
import com.xera.clientmanagement.service.AuthenticationService;
import com.xera.clientmanagement.service.JwtService;
import lombok.RequiredArgsConstructor;
import com.xera.clientmanagement.utils.encryptionUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final encryptionUtil encryptionUtil; // Ensure this is autowired correctly

    public Doctor signup(SignUpRequest signUpRequest) {
        Doctor doctor = new Doctor();

        // Encrypt email
        doctor.setEmail(encryptionUtil.encrypt(signUpRequest.getEmail()));
        doctor.setUsername(signUpRequest.getUsername()); // Optional
        doctor.setRole(Role.USER);
        doctor.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        return userRepository.save(doctor);
    }

    public JwtAuthenticationResponse signin(SignInRequest signInRequest) {
        String username = signInRequest.getUsername(); // Get username from request

        try {
            // Authenticate using username and password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, signInRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new InvalidLoginException("Invalid username or password.");
        }

        // Find the user using the username
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        // Generate JWT and refresh token
        var jwt = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

        // Create a response object
        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setToken(jwt);
        jwtAuthenticationResponse.setRefreshToken(refreshToken);
        jwtAuthenticationResponse.setDoctor(user); // Pass the doctor object (consider using a DTO)

        return jwtAuthenticationResponse;
    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String username = jwtService.extractUsername(refreshTokenRequest.getToken());
        Doctor doctor = userRepository.findByUsername(username).orElseThrow();
        if (jwtService.isTokenValid(refreshTokenRequest.getToken(), doctor)) {
            var jwt = jwtService.generateToken(doctor);

            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setToken(jwt);
            jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken());
            jwtAuthenticationResponse.setDoctor(doctor);
            return jwtAuthenticationResponse;
        }
        return null;
    }
}
