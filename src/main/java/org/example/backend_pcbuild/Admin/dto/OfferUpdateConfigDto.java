package org.example.backend_pcbuild.Admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class OfferUpdateConfigDto {
    private Integer intervalInMinutes;
    private OfferUpdateType type;
    private List<ShopDto> shops;
}
