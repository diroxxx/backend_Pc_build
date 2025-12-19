package org.project.backend_pcbuild.Game.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameDto {
    private Long id;
    private String title;
    private String imageUrl;

    private String minCpu;
    private String minGpu;
    private String recGpu;
    private String recCpu;

}
