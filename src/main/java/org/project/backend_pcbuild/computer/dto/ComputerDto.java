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
import org.project.backend_pcbuild.pcComponents.model.*;

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

        for (ComputerOffer computer_offer : ComputerOffers) {
            Offer offer = computer_offer.getOffer();
            Component c = offer.getComponent();
            if (c == null) continue;
            BaseOfferDto dto = switch (c) {
                case Processor p    -> OfferComponentMapper.toDto(p, offer);
                case GraphicsCard g -> OfferComponentMapper.toDto(g, offer);
                case Motherboard mb -> OfferComponentMapper.toDto(mb, offer);
                case Memory m       -> OfferComponentMapper.toDto(m, offer);
                case Storage s      -> OfferComponentMapper.toDto(s, offer);
                case PowerSupply ps -> OfferComponentMapper.toDto(ps, offer);
                case Case cs        -> OfferComponentMapper.toDto(cs, offer);
                case Cooler co      -> OfferComponentMapper.toDto(co, offer);
                default             -> null;
            };
            if (dto != null) componentDtos.add(dto);
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
