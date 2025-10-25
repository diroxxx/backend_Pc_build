package org.example.backend_pcbuild.Component.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoolerItemDto extends BaseItemDto{
    private List<String> coolerSocketsType;

}
