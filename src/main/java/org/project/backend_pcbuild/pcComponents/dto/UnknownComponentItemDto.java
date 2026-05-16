package org.project.backend_pcbuild.pcComponents.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.project.backend_pcbuild.offer.dto.BaseOfferDto;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;

@Data
@EqualsAndHashCode(callSuper = true)
public class UnknownComponentItemDto extends BaseOfferDto {

    {
        setComponentType(ComponentType.UNKNOWN);
    }
}
