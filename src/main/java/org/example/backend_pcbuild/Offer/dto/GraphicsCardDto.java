package org.example.backend_pcbuild.Offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.backend_pcbuild.models.ComponentType;

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