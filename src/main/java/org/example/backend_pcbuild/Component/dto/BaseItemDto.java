package org.example.backend_pcbuild.Component.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend_pcbuild.models.ComponentType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseItemDto {
    private Long id;
    private String model;
    private String brand;
    private ComponentType componentType;
}
