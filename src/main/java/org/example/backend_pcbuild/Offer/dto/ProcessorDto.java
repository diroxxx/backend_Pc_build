package org.example.backend_pcbuild.Offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessorDto extends BaseOfferDto {
    private Integer cores;
    private Integer threads;
    private String socketType;
    private String baseClock;


}
