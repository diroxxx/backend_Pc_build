package org.project.backend_pcbuild.pcComponents.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("GRAPHICS_CARD")
@Table(name = "graphics_card")
public class GraphicsCard extends Component {

    private Integer vram;
    private String gddr;
    private Double boostClock;
    private Double coreClock;
    private Double powerDraw;
    private Double lengthInMM;
    private Double benchmark;

    @ManyToOne
    @JoinColumn(name = "gpu_model_id")
    private GpuModel gpuModel;

}