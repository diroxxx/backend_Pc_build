package org.example.backend_pcbuild.Admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComponentOfferDto {
    private String title;
    private String brand;
    private String category;
    private String img;
    private String model;
    private Double price;
    private String shop;
    private String status;
    private String url;
}
