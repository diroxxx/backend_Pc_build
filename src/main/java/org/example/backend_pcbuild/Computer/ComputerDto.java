package org.example.backend_pcbuild.Computer;

import lombok.Builder;
import lombok.Data;
import org.example.backend_pcbuild.Components.dto.BaseComponentDto;
import org.example.backend_pcbuild.Components.dto.ComponentMapper;
import org.example.backend_pcbuild.models.*;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class ComputerDto {
    private String name;
    private Double price;
    private Boolean isVisible;

    private Set<BaseComponentDto> components = new HashSet<>();


    public static ComputerDto mapFromEntity(Computer computer) {
        Set<BaseComponentDto> componentDtos = new HashSet<>();

        Set<ComputerOffer> ComputerOffers = new HashSet<>(computer.getComputer_offer());

        for(ComputerOffer computer_offer: ComputerOffers){
            Offer offer = computer_offer.getOffer();
            if (offer.getItem().getProcessor() != null){
              componentDtos.add(ComponentMapper.toDto(offer.getItem().getProcessor(), offer));
            } else if (offer.getItem().getGraphicsCard() != null) {
                componentDtos.add(ComponentMapper.toDto(offer.getItem().getGraphicsCard(), offer));
            } else if (offer.getItem().getMotherboard() != null) {
                componentDtos.add(ComponentMapper.toDto(offer.getItem().getMotherboard(), offer));
            } else if (offer.getItem().getMemory() != null) {
                componentDtos.add(ComponentMapper.toDto(offer.getItem().getMemory(), offer));
            } else if (offer.getItem().getStorage() != null) {
                componentDtos.add(ComponentMapper.toDto(offer.getItem().getStorage(), offer));
            } else if (offer.getItem().getPowerSupply() != null) {
                componentDtos.add(ComponentMapper.toDto(offer.getItem().getPowerSupply(), offer));
            } else if (offer.getItem().getCase_() != null) {
                componentDtos.add(ComponentMapper.toDto(offer.getItem().getCase_(), offer));
            } else if (offer.getItem().getCooler() != null) {
                componentDtos.add(ComponentMapper.toDto(offer.getItem().getCooler(), offer));
            }
        }
        return ComputerDto.builder()
                .name(computer.getName())
                .price(computer.getPrice())
                .isVisible(computer.getIs_visible())
                .components(componentDtos)
                .build();
    }




}
