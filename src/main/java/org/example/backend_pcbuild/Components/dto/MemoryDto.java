package org.example.backend_pcbuild.Components.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemoryDto extends BaseComponentDto {
    private String type;
    private Integer capacity;
    private String speed;
    private String latency;

    @Override
    public String getComponentType() { return "memory"; }
}