package org.example.backend_pcbuild.Component.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class StorageItemDto extends BaseItemDto{
    private Double capacity;
}
