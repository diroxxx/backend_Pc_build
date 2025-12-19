package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Memory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private Integer capacity;
    private Integer speed;
    private Integer latency;
    private Integer amount;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "component_id")
    private Component component;

}
