package com.xera.clientmanagement.dto;

import com.xera.clientmanagement.entity.Doctor;
import lombok.Data;

@Data
public class JwtAuthenticationResponse {
    private String token;
    private String refreshToken;
    private Doctor doctor;

}
