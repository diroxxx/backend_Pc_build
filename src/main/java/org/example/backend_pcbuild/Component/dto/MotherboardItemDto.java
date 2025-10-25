package org.example.backend_pcbuild.Component.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MotherboardItemDto extends BaseItemDto{
    private String chipset;
    private String socketType;
    private String memoryType;
    private String format;
    private Integer ramSlots;
    private Integer ramCapacity;
}
