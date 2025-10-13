package org.example.backend_pcbuild.Components.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend_pcbuild.models.ItemCondition;

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