package org.project.backend_pcbuild.pcComponents.model;

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
    private String format;
    private Integer ramSlots;
    private Integer ramCapacity;
    private String memoryType;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "component_id")
    private Component component;

}
