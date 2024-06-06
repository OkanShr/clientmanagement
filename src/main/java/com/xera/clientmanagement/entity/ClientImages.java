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
public class ClientImages {

    @Id
    @GeneratedValue
    private Long id;
    private String fileName;
    private String imageUrl;
    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonBackReference
    private Client client;

    // Constructors, getters, setters...
}