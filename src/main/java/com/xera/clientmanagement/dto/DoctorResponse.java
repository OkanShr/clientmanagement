package com.xera.clientmanagement.dto;

import com.xera.clientmanagement.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private String username;
    private String email;  // Decrypted
    private Role role;
}
