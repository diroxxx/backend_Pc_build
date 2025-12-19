package org.project.backend_pcbuild.computer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.project.backend_pcbuild.computer.model.Computer;
import org.project.backend_pcbuild.computer.model.ComputerOffer;
import org.project.backend_pcbuild.offer.dto.BaseOfferDto;
import org.project.backend_pcbuild.offer.dto.OfferComponentMapper;
import org.project.backend_pcbuild.offer.model.Offer;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComputerDto {
    private Long id;
    private String name;
    private Double price;
    private Boolean isVisible;

    private Set<BaseOfferDto> offers = new HashSet<>();


    public static ComputerDto mapFromEntity(Computer computer) {
        Set<BaseOfferDto> componentDtos = new HashSet<>();

        Set<ComputerOffer> ComputerOffers = new HashSet<>(computer.getComputer_offer());

        for(ComputerOffer computer_offer: ComputerOffers){
            Offer offer = computer_offer.getOffer();
            if (offer.getComponent().getProcessor() != null){
              componentDtos.add(OfferComponentMapper.toDto(offer.getComponent().getProcessor(), offer));
            } else if (offer.getComponent().getGraphicsCard() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getComponent().getGraphicsCard(), offer));
            } else if (offer.getComponent().getMotherboard() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getComponent().getMotherboard(), offer));
            } else if (offer.getComponent().getMemory() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getComponent().getMemory(), offer));
            } else if (offer.getComponent().getStorage() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getComponent().getStorage(), offer));
            } else if (offer.getComponent().getPowerSupply() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getComponent().getPowerSupply(), offer));
            } else if (offer.getComponent().getCase_() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getComponent().getCase_(), offer));
            } else if (offer.getComponent().getCooler() != null) {
                componentDtos.add(OfferComponentMapper.toDto(offer.getComponent().getCooler(), offer));
            }
        }
        return ComputerDto.builder()
                .id(computer.getId())
                .name(computer.getName())
                .price(computer.getPrice())
                .isVisible(computer.getIs_visible())
                .offers(componentDtos)
                .build();
    }




}
