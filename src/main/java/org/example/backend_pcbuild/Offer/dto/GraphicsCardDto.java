package org.example.backend_pcbuild.Offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GraphicsCardDto extends BaseOfferDto {
    private Integer vram;
    private String gddr;
    private Double powerDraw;

}