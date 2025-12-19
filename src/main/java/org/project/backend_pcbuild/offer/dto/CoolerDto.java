package org.project.backend_pcbuild.offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoolerDto extends BaseOfferDto {
    private List<String> coolerSocketsType;
    private String fanRpm;
    private String noiseLevel;
    private String radiatorSize;

    {
        setComponentType(ComponentType.CPU_COOLER);
    }

}
