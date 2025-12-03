package org.example.backend_pcbuild.Game;

import lombok.Data;
import org.example.backend_pcbuild.models.GpuModel;
import org.example.backend_pcbuild.models.RecGameLevel;

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
