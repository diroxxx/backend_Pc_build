package org.project.backend_pcbuild.Game.dto;

import lombok.Data;
import org.project.backend_pcbuild.pcComponents.model.GpuModel;

@Data
public class GpuRecDto {
    private Long gpuModelId;
    private String gpuModel;
    private RecGameLevel recGameLevel;


    public static GpuRecDto toDto(GpuModel gpuModel){
        GpuRecDto dto = new GpuRecDto();
        dto.setGpuModel(gpuModel.getChipset());
        dto.setGpuModelId(gpuModel.getId());
        return dto;
    }
}
