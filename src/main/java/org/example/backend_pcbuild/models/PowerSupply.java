package org.example.backend_pcbuild.models;

import jakarta.persistence.*;

@Entity
public class PowerSupply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int maxPowerWatt;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;
}
