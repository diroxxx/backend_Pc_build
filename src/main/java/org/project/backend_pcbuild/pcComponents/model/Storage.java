package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double capacity;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "component_id")
    private Component component;

}
