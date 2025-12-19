package org.project.backend_pcbuild.Game.dto;

import lombok.Data;
import org.project.backend_pcbuild.offer.dto.BaseOfferDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class CpuGpuRecGameDto {

//    List<OfferRecDto> minRec = new ArrayList<>();
//    List<OfferRecDto> maxRec = new ArrayList<>();

    List<BaseOfferDto> minRec = new ArrayList<>();
    List<BaseOfferDto> maxRec = new ArrayList<>();


}
