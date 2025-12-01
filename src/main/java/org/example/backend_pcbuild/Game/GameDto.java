package org.example.backend_pcbuild.Game;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameDto {
    private Long id;
    private String title;
    private String imageBase64;

    private String minCpu;
    private String minGpu;
    private String recGpu;
    private String recCpu;

}
