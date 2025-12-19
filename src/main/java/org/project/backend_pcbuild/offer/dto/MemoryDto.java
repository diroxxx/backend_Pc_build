package org.project.backend_pcbuild.offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;

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