package org.project.backend_pcbuild.Game.dto;

import lombok.Data;
import org.project.backend_pcbuild.pcComponents.model.Processor;

@Data
public class CpuRecDto {
    private Long processorId;
    private String processorModel;
    private RecGameLevel recGameLevel;


    public static CpuRecDto toDto(Processor processor){
        CpuRecDto cpuRecDto = new CpuRecDto();
        cpuRecDto.setProcessorId(processor.getId());
        cpuRecDto.setProcessorModel(processor.getComponent().getModel());
        return cpuRecDto;
    }
}
