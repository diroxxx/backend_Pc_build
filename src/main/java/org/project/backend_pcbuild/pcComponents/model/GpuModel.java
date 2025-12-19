package org.project.backend_pcbuild.pcComponents.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.project.backend_pcbuild.Game.model.GameGpuRequirements;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class GpuModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CsvBindByName(column = "chipset")
    private String chipset;

    @CsvIgnore
    @OneToMany(mappedBy = "gpuModel")
    private List<GraphicsCard> graphicsCards = new ArrayList<>();

    @CsvIgnore
    @OneToMany(mappedBy = "gpuModel")
    private List<GameGpuRequirements> gameGpuRequirements = new ArrayList<>();
}
