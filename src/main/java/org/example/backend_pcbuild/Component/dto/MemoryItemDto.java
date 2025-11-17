package org.example.backend_pcbuild.Component.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemoryItemDto extends BaseItemDto{
    private String type;
    private Integer capacity;
    private Integer speed;
    private Integer latency;
    private Integer amount;
}
