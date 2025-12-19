package org.project.backend_pcbuild.offer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ComponentStatsDto {
    private String componentType;
    private long total;
    private Map<String, Long> shopBreakdown;
}

