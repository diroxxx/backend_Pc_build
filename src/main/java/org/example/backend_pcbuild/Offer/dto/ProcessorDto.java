package org.example.backend_pcbuild.Offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.backend_pcbuild.models.ComponentType;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessorDto extends BaseOfferDto {
    private Integer cores;
    private Integer threads;
    private String socketType;
    private Double baseClock;
    private Double boostClock;
    private String integratedGraphics;
    private Integer tdp;

    {
        setComponentType(ComponentType.PROCESSOR);
    }


}
