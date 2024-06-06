package com.xera.clientmanagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    private String activity;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date date;

    private String time;

    @ManyToOne
    @JoinColumn(name = "clientId", nullable = false)
    @JsonBackReference
    private Client client;

    @OneToMany(mappedBy = "appointment", cascade = {CascadeType.ALL})
    @JsonManagedReference
    private List<AppointmentPdf> appointmentPdfs;

    private String type;
}
