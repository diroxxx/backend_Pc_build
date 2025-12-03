package org.example.backend_pcbuild.Game;

import lombok.Data;
import org.example.backend_pcbuild.models.Processor;
import org.example.backend_pcbuild.models.RecGameLevel;

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
