package com.xera.clientmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {
    private Long id;
    private String activity;
    private Date date;
    private String time;
    private Long clientId;
    private String type;

}
