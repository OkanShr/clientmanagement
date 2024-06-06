package com.xera.clientmanagement.dto;

import com.xera.clientmanagement.entity.Doctor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private String phoneNumber;
    private Long doctorId;
}
