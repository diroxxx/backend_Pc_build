package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "pc_case")
public class Case {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String format;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "component_id")
    private Component component;

}
