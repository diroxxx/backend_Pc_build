package org.project.backend_pcbuild.pcComponents.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MotherboardItemDto extends BaseItemDto{
    private String chipset;
    private String socketType;
    private String format;
    private Integer ramSlots;
    private Integer ramCapacity;
    private String memoryType;
}
