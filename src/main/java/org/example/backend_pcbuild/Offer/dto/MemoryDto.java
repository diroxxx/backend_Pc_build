package org.example.backend_pcbuild.Offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.backend_pcbuild.models.ComponentType;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemoryDto extends BaseOfferDto {
    private String type;
    private Integer capacity;
    private Integer speed;
    private Integer latency;
    private Integer amount;

    {
        setComponentType(ComponentType.MEMORY);
    }
}