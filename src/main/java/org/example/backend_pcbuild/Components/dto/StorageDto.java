package org.example.backend_pcbuild.Components.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StorageDto extends BaseComponentDto {
    private Double capacity;

    @Override
    public String getComponentType() { return "storage"; }
}
