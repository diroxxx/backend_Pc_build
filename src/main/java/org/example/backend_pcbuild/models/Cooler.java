package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Cooler {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String socketType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "item_id")
    private Item item;



}
