package org.project.backend_pcbuild.offersUpdates.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class OfferUpdateStatsDTO {
    private Long offerCount;
    private LocalDate dateOfUpdate;

    public OfferUpdateStatsDTO(Long offerCount, LocalDateTime dateTime) {
        this.offerCount = offerCount;
        this.dateOfUpdate = dateTime.toLocalDate();
    }
}
