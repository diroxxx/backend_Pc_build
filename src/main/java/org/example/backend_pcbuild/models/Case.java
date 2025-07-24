package org.example.backend_pcbuild.models;

import jakarta.persistence.*;

@Entity
public class Case {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String format;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

}
