package org.example.backend_pcbuild.Components.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoolerDto extends BaseComponentDto {
    private List<String> coolerSocketsType;

    @Override
    public String getComponentType() {
        return "cooler";
    }
}
