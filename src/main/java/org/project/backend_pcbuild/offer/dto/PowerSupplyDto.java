package org.project.backend_pcbuild.offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;

@Data
@EqualsAndHashCode(callSuper = true)
public class PowerSupplyDto extends BaseOfferDto {
    private String modular;
    private String type;
    private String efficiencyRating;
    private Integer maxPowerWatt;    {
        setComponentType(ComponentType.POWER_SUPPLY);
    }

}