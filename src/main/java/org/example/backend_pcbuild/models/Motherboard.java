package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Motherboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chipset;
    private String socketType;
    private String memoryType;
    private String format;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "item_id")
    private Item item;

}
