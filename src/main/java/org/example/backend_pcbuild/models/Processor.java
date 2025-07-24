package org.example.backend_pcbuild.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Processor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int cores;
    private int threads;
    private String socket_type;
    private double base_clock;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

}
