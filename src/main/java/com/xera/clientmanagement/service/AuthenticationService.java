package com.xera.clientmanagement.service;

import com.xera.clientmanagement.dto.JwtAuthenticationResponse;
import com.xera.clientmanagement.dto.RefreshTokenRequest;
import com.xera.clientmanagement.dto.SignInRequest;
import com.xera.clientmanagement.dto.SignUpRequest;
import com.xera.clientmanagement.entity.Doctor;

public interface AuthenticationService {

    Doctor signup(SignUpRequest signUpRequest);

    JwtAuthenticationResponse signin(SignInRequest signInRequest);

    JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}
