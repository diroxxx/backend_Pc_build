package org.example.backend_pcbuild.Computer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CaseDto extends BaseComponentDto {
    private String format;

    @Override
    public String getComponentType() { return "casePc"; }
}