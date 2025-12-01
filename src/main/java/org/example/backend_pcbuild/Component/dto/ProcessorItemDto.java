package org.example.backend_pcbuild.Component.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessorItemDto  extends BaseItemDto{
    private Integer cores;
    private Integer threads;
    private String socketType;
    private Double baseClock;
    private Double boostClock;
    private String integratedGraphics;
    private Integer tdp;

    private Double benchmark;
}
