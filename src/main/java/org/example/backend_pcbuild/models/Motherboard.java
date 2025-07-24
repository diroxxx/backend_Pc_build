package org.example.backend_pcbuild.models;

import jakarta.persistence.*;

@Entity
public class Motherboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chipset;
    private String socketType;
    private String memoryType;
    private String format;


    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

}
