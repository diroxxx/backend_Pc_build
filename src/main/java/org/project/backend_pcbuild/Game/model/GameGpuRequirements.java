package org.project.backend_pcbuild.Game.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.project.backend_pcbuild.pcComponents.model.GpuModel;
import org.project.backend_pcbuild.Game.dto.RecGameLevel;

@Data
@Entity
public class GameGpuRequirements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RecGameLevel recGameLevel;

    @ManyToOne
    @JoinColumn(name = "gpu_model_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GpuModel gpuModel;

    @ManyToOne
    @JoinColumn(name = "game_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Game game;
}
