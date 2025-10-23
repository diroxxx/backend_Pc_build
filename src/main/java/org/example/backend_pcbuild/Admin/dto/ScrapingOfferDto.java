package org.example.backend_pcbuild.Admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
