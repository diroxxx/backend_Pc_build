package org.example.backend_pcbuild;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Processor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int cores;
    private int threads;
    private String socket_type;
    private double base_clock;
    private double power_draw;



}
