package org.example.backend_pcbuild.Admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend_pcbuild.models.ComponentType;

import java.io.IOException;

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
