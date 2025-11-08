package org.example.backend_pcbuild.Offer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.backend_pcbuild.models.ComponentType;

@Data
@EqualsAndHashCode(callSuper = true)
public class StorageDto extends BaseOfferDto {
    private Double capacity;

    {
        setComponentType(ComponentType.STORAGE);
    }
}
