package org.project.backend_pcbuild.offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;

@Data
@EqualsAndHashCode(callSuper = true)
public class MotherboardDto extends BaseOfferDto {
    private String chipset;
    private String socketType;
    private String format;
    private Integer ramSlots;
    private Integer ramCapacity;
    private String memoryType;

    {
        setComponentType(ComponentType.MOTHERBOARD);
    }


}