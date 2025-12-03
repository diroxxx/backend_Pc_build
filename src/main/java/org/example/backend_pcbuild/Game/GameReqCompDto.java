package org.example.backend_pcbuild.Game;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameReqCompDto {
    private Long id;
    private String title;
    private String imageUrl;
    List<CpuRecDto> cpuSpecs = new ArrayList<>();
    List<GpuRecDto> gpuSpecs = new ArrayList<>();



}
