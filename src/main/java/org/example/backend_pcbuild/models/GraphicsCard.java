package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class GraphicsCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer memorySize;
    private String gddr;
    private Double power_draw;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "item_id")
    private Item item;


}
