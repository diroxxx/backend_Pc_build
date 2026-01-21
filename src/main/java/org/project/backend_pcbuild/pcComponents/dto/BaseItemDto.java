package org.project.backend_pcbuild.pcComponents.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "componentType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProcessorItemDto.class, name = "PROCESSOR"),
        @JsonSubTypes.Type(value = GraphicsCardItemDto.class, name = "GRAPHICS_CARD"),
        @JsonSubTypes.Type(value = MemoryItemDto.class, name = "MOTHERBOARD"),
        @JsonSubTypes.Type(value = MemoryItemDto.class, name = "MEMORY"),
        @JsonSubTypes.Type(value = CaseItemDto.class, name = "CASE_PC"),
        @JsonSubTypes.Type(value = StorageItemDto.class, name = "STORAGE"),
        @JsonSubTypes.Type(value = PowerSupplyItemDto.class, name = "POWER_SUPPLY"),
        @JsonSubTypes.Type(value = CoolerItemDto.class, name = "CPU_COOLER")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseItemDto {
    private Long id;
    private String model;
    private String brand;
    private ComponentType componentType;
}
