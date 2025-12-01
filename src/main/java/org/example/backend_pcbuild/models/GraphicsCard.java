package org.example.backend_pcbuild.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class GraphicsCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer vram;
    private String gddr;
    private Double boostClock;
    private Double coreClock;
    private Double powerDraw;
    private Double lengthInMM;

    private Double benchmark;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "component_id")
    private Component component;

    @ManyToOne
    @JoinColumn(name = "gpu_model_id")
    private GpuModel gpuModel;

}
