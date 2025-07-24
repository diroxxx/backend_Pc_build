package org.example.backend_pcbuild.models;

import jakarta.persistence.*;

@Entity
public class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double capacity;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;


}
