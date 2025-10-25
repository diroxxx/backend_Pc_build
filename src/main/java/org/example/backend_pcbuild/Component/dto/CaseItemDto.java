package org.example.backend_pcbuild.Component.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CaseItemDto extends BaseItemDto{
    private String format;
}
