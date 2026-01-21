package org.project.backend_pcbuild.pcComponents.dto;

import com.opencsv.bean.CsvBindAndSplitByName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoolerItemDto extends BaseItemDto{

    @CsvBindAndSplitByName(column = "socket", elementType = String.class, splitOn = ",", collectionType = java.util.ArrayList.class)
    private List<String> coolerSocketsType;
    private String fanRpm;
    private String noiseLevel;
    private String radiatorSize;

}
