package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.*;
import lombok.Data;
import org.project.backend_pcbuild.Game.model.GameCpuRequirements;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Processor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer cores;
    private Integer threads;
    private String socketType;
    private Double baseClock;
    private Double boostClock;
    private String integratedGraphics;
    private Integer tdp;

    private Double benchmark;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "component_id")
    private Component component;

    @OneToMany(mappedBy = "processor", cascade = CascadeType.ALL)
    private List<GameCpuRequirements> gameCpuRequirements = new ArrayList<>();

}
