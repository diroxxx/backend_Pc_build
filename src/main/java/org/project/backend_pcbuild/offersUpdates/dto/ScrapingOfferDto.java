package org.project.backend_pcbuild.offersUpdates.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.backend_pcbuild.offer.dto.ComponentOfferDto;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScrapingOfferDto {
    private Long updateId;
    private String shopName;
    private List<ComponentOfferDto> componentsData;
}
