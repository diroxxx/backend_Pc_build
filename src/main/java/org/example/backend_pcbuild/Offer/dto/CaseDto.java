package org.example.backend_pcbuild.Offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.backend_pcbuild.models.ComponentType;

@Data
@EqualsAndHashCode(callSuper = true)
public class CaseDto extends BaseOfferDto {
    private String format;

    {
        setComponentType(ComponentType.CASE_PC);
    }

}