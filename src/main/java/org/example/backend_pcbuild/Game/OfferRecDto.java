package org.example.backend_pcbuild.Game;

import lombok.Data;
import org.example.backend_pcbuild.models.ComponentType;
import org.example.backend_pcbuild.models.ComponentCondition;
import org.example.backend_pcbuild.models.Offer;

@Data
public class OfferRecDto {
    private String title;
    private String brand;
    private String model;
    private ComponentCondition condition;
    private String photoUrl;
    private String websiteUrl;
    private Double price;
    private String shopName;
    private ComponentType componentType;


  public  static OfferRecDto toDto(Offer offer){
        OfferRecDto dto = new OfferRecDto();
        dto.setBrand(offer.getComponent().getBrand().getName());
        dto.setModel(offer.getComponent().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPrice(offer.getPrice());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setTitle(offer.getTitle());
        dto.setShopName(offer.getShop().getName());
        dto.setComponentType(offer.getComponent().getComponentType());
        return dto;
    }
}
