package org.project.backend_pcbuild.offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;

@Data
@EqualsAndHashCode(callSuper = true)
public class GraphicsCardDto extends BaseOfferDto {
    private Integer vram;
    private String gddr;
    private Double boostClock;
    private Double coreClock;
    private Double powerDraw;
    private Double lengthInMM;

    {
        setComponentType(ComponentType.GRAPHICS_CARD);
    }

}