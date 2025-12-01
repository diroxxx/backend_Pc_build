package org.example.backend_pcbuild.YoutubeGameRecomendation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameFpsConfigDto {
    private String gameTitle;
    private String resolution;
    private String graphicsPreset;
    private String technology;
    private String cpu;
    private String gpu;
    private Double budget;

}
