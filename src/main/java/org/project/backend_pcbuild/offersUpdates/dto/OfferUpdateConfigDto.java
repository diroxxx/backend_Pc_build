package org.project.backend_pcbuild.offersUpdates.dto;

import lombok.Data;
import org.project.backend_pcbuild.offersUpdates.model.OfferUpdateType;

import java.util.List;

@Data
public class OfferUpdateConfigDto {
    private Integer intervalInMinutes;
    private OfferUpdateType type;
    private List<ShopDto> shops;
}
