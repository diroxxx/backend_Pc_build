package org.example.backend_pcbuild.Component.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GraphicsCardItemDto extends BaseItemDto {
    private Integer vram;
    private String gddr;
    private Integer boostClock;
    private Integer coreClock;
    private Double powerDraw;
    private Double lengthInMM;
}
