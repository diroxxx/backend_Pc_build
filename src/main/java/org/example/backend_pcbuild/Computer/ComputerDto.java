package org.example.backend_pcbuild.Computer;

import lombok.Builder;
import lombok.Data;
import org.example.backend_pcbuild.Offer.dto.BaseOfferDto;
import org.example.backend_pcbuild.Offer.dto.OfferComponentMapper;
import org.example.backend_pcbuild.models.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class ComputerDto {
    private String name;
    private Double price;
    private Boolean isVisible;

    private Set<BaseOfferDto> offers = new HashSet<>();


    public static ComputerDto mapFromEntity(Computer computer) {
        Set<BaseOfferDto> componentDtos = new HashSet<>();

        Set<ComputerOffer> ComputerOffers = new HashSet<>(computer.getComputer_offer());

        for(ComputerOffer computer_offer: ComputerOffers){
            Offer offer = computer_offer.getOffer();
            if (offer.getItem().getProcessor() != null){
              componentDtos.add(OfferComponentMapper.toDto(offer.getItem().getProcessor(), offer));
            } else if (offer.getItem().getGraphicsCard() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getItem().getGraphicsCard(), offer));
            } else if (offer.getItem().getMotherboard() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getItem().getMotherboard(), offer));
            } else if (offer.getItem().getMemory() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getItem().getMemory(), offer));
            } else if (offer.getItem().getStorage() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getItem().getStorage(), offer));
            } else if (offer.getItem().getPowerSupply() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getItem().getPowerSupply(), offer));
            } else if (offer.getItem().getCase_() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getItem().getCase_(), offer));
            } else if (offer.getItem().getCooler() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getItem().getCooler(), offer));
            }
        }
        return ComputerDto.builder()
                .name(computer.getName())
                .price(computer.getPrice())
                .isVisible(computer.getIs_visible())
                .offers(componentDtos)
                .build();
    }




}
