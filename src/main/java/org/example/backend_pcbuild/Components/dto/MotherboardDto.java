package org.example.backend_pcbuild.Components.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MotherboardDto extends BaseComponentDto {
    private String chipset;
    private String socketType;
    private String memoryType;
    private String format;
    private Integer ramSlots;
    private Integer ramCapacity;

    @Override
    public String getComponentType() { return "motherboard"; }
}