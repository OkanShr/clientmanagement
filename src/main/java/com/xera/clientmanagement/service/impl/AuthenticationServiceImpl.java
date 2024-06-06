package com.xera.clientmanagement.service.impl;

import com.xera.clientmanagement.dto.JwtAuthenticationResponse;
import com.xera.clientmanagement.dto.RefreshTokenRequest;
import com.xera.clientmanagement.dto.SignInRequest;
import com.xera.clientmanagement.dto.SignUpRequest;
import com.xera.clientmanagement.entity.Doctor;
import com.xera.clientmanagement.entity.Role;
import com.xera.clientmanagement.repository.UserRepository;
import com.xera.clientmanagement.service.AuthenticationService;
import com.xera.clientmanagement.service.JwtService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public Doctor signup(SignUpRequest signUpRequest){
        Doctor doctor = new Doctor();

        doctor.setEmail(signUpRequest.getEmail());
        doctor.setUsername(signUpRequest.getUsername());
        doctor.setRole(Role.USER);
        doctor.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        return userRepository.save(doctor);
    }


    public JwtAuthenticationResponse signin(SignInRequest signInRequest){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getEmail(),signInRequest.getPassword()));

        var user = userRepository.findByEmail(signInRequest.getEmail()).orElseThrow(()-> new IllegalArgumentException("Invalid email or password."));

        var jwt = jwtService.generateToken(user);

        var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();

        jwtAuthenticationResponse.setToken(jwt);
        jwtAuthenticationResponse.setRefreshToken(refreshToken);
        jwtAuthenticationResponse.setDoctor(user);
        return jwtAuthenticationResponse;
    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest){
        String userEmail = jwtService.extractUsername(refreshTokenRequest.getToken());
        Doctor doctor = userRepository.findByEmail(userEmail).orElseThrow();
        if(jwtService.isTokenValid(refreshTokenRequest.getToken(), doctor)){
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
