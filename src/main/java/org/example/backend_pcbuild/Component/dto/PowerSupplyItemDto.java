package org.example.backend_pcbuild.Component.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PowerSupplyItemDto extends BaseItemDto{
    private Integer maxPowerWatt;

}
