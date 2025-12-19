package org.project.backend_pcbuild.pcComponents.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoolerItemDto extends BaseItemDto{

//    @CsvBindAndSplitByName(
//            column = "coolerSocketsType",
//            elementType = String.class,
//            splitOn = ";",
//            writeDelimiter = ";"
//    )
    private List<String> coolerSocketsType;
    private String fanRpm;
    private String noiseLevel;
    private String radiatorSize;

}
