package com.xera.clientmanagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointment_pdf")
public class AppointmentPdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    @JsonBackReference
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "pdf_id")
    @JsonBackReference
    private PdfFile pdfFile;

    private String pdfType;
}
