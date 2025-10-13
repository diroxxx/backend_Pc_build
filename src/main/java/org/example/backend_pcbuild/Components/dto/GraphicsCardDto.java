package org.example.backend_pcbuild.Components.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GraphicsCardDto extends BaseComponentDto {
    private Integer vram;
    private String gddr;
    private Double powerDraw;

    @Override
    public String getComponentType() { return "graphicsCard"; }
}