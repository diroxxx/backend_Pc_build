package org.project.backend_pcbuild.offer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComponentOfferDto {
    private String title;
    private String brand;


    @JsonProperty("category")
    @JsonDeserialize(using = ComponentTypeDeserializer.class)
    private ComponentType category;

    private String img;
    private String model;
    private Double price;
    private String shop;
    private String status;
    private String url;
}
