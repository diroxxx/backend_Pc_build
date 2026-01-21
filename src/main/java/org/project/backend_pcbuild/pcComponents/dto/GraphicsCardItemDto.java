package org.project.backend_pcbuild.pcComponents.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GraphicsCardItemDto extends BaseItemDto {
    private Integer vram;
    private String gddr;
    private Double boostClock;
    private Double coreClock;
    private Double powerDraw;
    private Double lengthInMM;
    private String baseModel;
    private Double benchmark;

}
