package org.project.backend_pcbuild.offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;

@Data
@EqualsAndHashCode(callSuper = true)
public class CaseDto extends BaseOfferDto {
    private String format;

    {
        setComponentType(ComponentType.CASE_PC);
    }

}