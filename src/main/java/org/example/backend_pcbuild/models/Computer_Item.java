package org.example.backend_pcbuild.models;

import jakarta.persistence.*;

@Entity
public class Computer_Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "computer_id", nullable = false)
    private Computer computer;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

}
