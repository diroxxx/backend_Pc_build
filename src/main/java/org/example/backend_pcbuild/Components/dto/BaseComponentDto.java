package org.example.backend_pcbuild.Components.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend_pcbuild.models.ItemCondition;



@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProcessorDto.class, name = "processor"),
        @JsonSubTypes.Type(value = GraphicsCardDto.class, name = "graphicsCard"),
        @JsonSubTypes.Type(value = MotherboardDto.class, name = "motherboard"),
        @JsonSubTypes.Type(value = MemoryDto.class, name = "memory"),
        @JsonSubTypes.Type(value = PowerSupplyDto.class, name = "powerSupply"),
        @JsonSubTypes.Type(value = CoolerDto.class, name = "cooler"),
        @JsonSubTypes.Type(value = CaseDto.class, name = "casePc")
})

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseComponentDto {
    private String brand;
    private String model;
    private ItemCondition condition;
    private String photoUrl;
    private String websiteUrl;
    private Double price;
    private String shop;

    public abstract String getComponentType();
}