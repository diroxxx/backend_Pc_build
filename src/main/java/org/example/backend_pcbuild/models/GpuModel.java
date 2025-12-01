package org.example.backend_pcbuild.models;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;
import jakarta.persistence.*;
import lombok.Data;

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
