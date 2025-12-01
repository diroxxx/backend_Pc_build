package org.example.backend_pcbuild.Game;

import lombok.Data;
import org.example.backend_pcbuild.Offer.dto.BaseOfferDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class CpuGpuRecGameDto {

    List<OfferRecDto> minRec = new ArrayList<>();
    List<OfferRecDto> maxRec = new ArrayList<>();

}
