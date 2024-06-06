package com.xera.clientmanagement.controller;

import com.xera.clientmanagement.dto.JwtAuthenticationResponse;
import com.xera.clientmanagement.dto.RefreshTokenRequest;
import com.xera.clientmanagement.dto.SignInRequest;
import com.xera.clientmanagement.dto.SignUpRequest;
import com.xera.clientmanagement.entity.Doctor;
import com.xera.clientmanagement.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

//@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/signup")
    public ResponseEntity<Doctor> signup(@RequestBody SignUpRequest signUpRequest){
        return ResponseEntity.ok(authenticationService.signup(signUpRequest));
    }


    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SignInRequest signInRequest){
        return ResponseEntity.ok(authenticationService.signin(signInRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest){
        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenRequest));
    }

}
