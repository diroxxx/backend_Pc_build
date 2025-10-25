package org.example.backend_pcbuild.Component.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProcessorItemDto  extends BaseItemDto{
    private Integer cores;
    private Integer threads;
    private String socketType;
    private String baseClock;
}
