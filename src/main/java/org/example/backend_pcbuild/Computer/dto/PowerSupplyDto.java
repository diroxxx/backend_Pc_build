package org.example.backend_pcbuild.Computer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PowerSupplyDto extends BaseComponentDto {
    private Integer maxPowerWatt;

    @Override
    public String getComponentType() { return "powerSupply"; }
}