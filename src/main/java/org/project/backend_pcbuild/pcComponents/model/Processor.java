package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.project.backend_pcbuild.Game.model.GameCpuRequirements;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("PROCESSOR")
@Table(name = "processor")
public class Processor extends Component {

    private Integer cores;
    private Integer threads;
    private String socketType;
    private Double baseClock;
    private Double boostClock;
    private String integratedGraphics;
    private Integer tdp;
    private Double benchmark;

    @OneToMany(mappedBy = "processor")
    private List<GameCpuRequirements> gameCpuRequirements = new ArrayList<>();

}