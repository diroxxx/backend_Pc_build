package org.project.backend_pcbuild.pcComponents.dto;

import org.project.backend_pcbuild.pcComponents.model.*;
import org.project.backend_pcbuild.pcComponents.model.*;
import org.springframework.stereotype.Component;

@Component
public class ItemComponentMapper {

    public ProcessorItemDto toDto(Processor entity) {
        ProcessorItemDto dto = new ProcessorItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ComponentType.PROCESSOR);
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setSocketType(entity.getSocketType());
        dto.setBaseClock(entity.getBaseClock());
        dto.setCores(entity.getCores());
        dto.setThreads(entity.getThreads());
        dto.setTdp(entity.getTdp());
        dto.setBoostClock(entity.getBoostClock());
        dto.setIntegratedGraphics(entity.getIntegratedGraphics());
        return dto;
    }

    public GraphicsCardItemDto toDto(GraphicsCard entity) {
        GraphicsCardItemDto dto = new GraphicsCardItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ComponentType.GRAPHICS_CARD);
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setVram(entity.getVram());
        dto.setGddr(entity.getGddr());
        dto.setPowerDraw(entity.getPowerDraw());
        return dto;
    }

    public MemoryItemDto toDto(Memory entity) {
        MemoryItemDto dto = new MemoryItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ComponentType.MEMORY);
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setCapacity(entity.getCapacity());
        dto.setType(entity.getType());
        dto.setLatency(entity.getLatency());
        dto.setSpeed(entity.getSpeed());
        return dto;
    }

    public MotherboardItemDto toDto(Motherboard entity) {
        MotherboardItemDto dto = new MotherboardItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ComponentType.MOTHERBOARD);
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setChipset(entity.getChipset());
        dto.setFormat(entity.getFormat());
        dto.setMemoryType(entity.getMemoryType());
        dto.setSocketType(entity.getSocketType());
        dto.setRamCapacity(entity.getRamCapacity());
        dto.setRamSlots(entity.getRamSlots());
        return dto;
    }

    public PowerSupplyItemDto toDto(PowerSupply entity) {
        PowerSupplyItemDto dto = new PowerSupplyItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ComponentType.POWER_SUPPLY);
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setMaxPowerWatt(entity.getMaxPowerWatt());
        return dto;
    }

    public StorageItemDto toDto(Storage entity) {
        StorageItemDto dto = new StorageItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ComponentType.STORAGE);
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setCapacity(entity.getCapacity());
        return dto;
    }

    public CaseItemDto toDto(Case entity) {
        CaseItemDto dto = new CaseItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ComponentType.CASE_PC);
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setFormat(entity.getFormat());
        return dto;
    }
    public CoolerItemDto toDto(Cooler entity) {
        CoolerItemDto dto = new CoolerItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ComponentType.CPU_COOLER);
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setCoolerSocketsType(entity.getSocketTypes());
        return dto;
    }


}
