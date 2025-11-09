package org.example.backend_pcbuild.Offer.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend_pcbuild.models.ComponentType;
import org.example.backend_pcbuild.models.ItemCondition;



@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "componentType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProcessorDto.class, name = "PROCESSOR"),
        @JsonSubTypes.Type(value = GraphicsCardDto.class, name = "GRAPHICS_CARD"),
        @JsonSubTypes.Type(value = MotherboardDto.class, name = "MOTHERBOARD"),
        @JsonSubTypes.Type(value = MemoryDto.class, name = "MEMORY"),
        @JsonSubTypes.Type(value = PowerSupplyDto.class, name = "POWER_SUPPLY"),
        @JsonSubTypes.Type(value = CoolerDto.class, name = "CPU_COOLER"),
        @JsonSubTypes.Type(value = CaseDto.class, name = "CASE_PC")
})

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseOfferDto {
    private Long id;
    private String title;
    private String brand;
    private String model;
    private ItemCondition condition;
    private String photoUrl;
    private String websiteUrl;
    private Double price;
    private String shopName;
    private ComponentType componentType;


}