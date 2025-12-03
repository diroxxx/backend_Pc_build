package org.example.backend_pcbuild.Game;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

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
