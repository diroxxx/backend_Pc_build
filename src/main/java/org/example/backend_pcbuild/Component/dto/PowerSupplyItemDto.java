package org.example.backend_pcbuild.Component.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PowerSupplyItemDto extends BaseItemDto{
    private String modular;
    private String type;
    private String efficiencyRating;
    private Integer maxPowerWatt;
}
