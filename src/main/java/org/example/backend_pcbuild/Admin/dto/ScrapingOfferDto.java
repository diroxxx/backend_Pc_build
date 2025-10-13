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
    @JsonProperty("components_data")
    private List<ComponentOfferDto> componentsData;

    @JsonProperty("finished_at")
    private LocalDateTime finishedAt;

    private String name;

    @JsonProperty("offers_added")
    private Integer offersAdded;

    @JsonProperty("offers_deleted")
    private Integer offersDeleted;

    @JsonProperty("offers_updated")
    private Integer offersUpdated;

    @JsonProperty("started_at")
    private LocalDateTime startedAt;
}
