package org.example.backend_pcbuild.Components.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessorDto extends BaseComponentDto {
    private Integer cores;
    private Integer threads;
    private String socketType;
    private String baseClock;

    @Override
    public String getComponentType() { return "processor"; }
}
