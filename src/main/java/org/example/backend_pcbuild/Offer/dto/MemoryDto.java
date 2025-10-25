package org.example.backend_pcbuild.Offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemoryDto extends BaseOfferDto {
    private String type;
    private Integer capacity;
    private String speed;
    private String latency;

}