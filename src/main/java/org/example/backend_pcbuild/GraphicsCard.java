package org.example.backend_pcbuild;

import jakarta.persistence.*;

@Entity
public class GraphicsCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int memorySize;
    private String gddr;
    private double power_draw;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;


}
