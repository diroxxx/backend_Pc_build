package org.example.backend_pcbuild.models;

import jakarta.persistence.*;

@Entity
public class Memory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private int capacity;
    private int speed;
    private String latency;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

}
