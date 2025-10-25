package org.example.backend_pcbuild.Component.dto;

import org.example.backend_pcbuild.Offer.dto.*;
import org.example.backend_pcbuild.models.*;
import org.springframework.stereotype.Component;

@Component
public class ItemComponentMapper {

    public ProcessorItemDto toDto(Processor entity) {
        ProcessorItemDto dto = new ProcessorItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ItemType.PROCESSOR);
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setSocketType(entity.getSocket_type());
        dto.setBaseClock(entity.getBase_clock());
        dto.setCores(entity.getCores());
        dto.setThreads(entity.getThreads());
        return dto;
    }

    public GraphicsCardItemDto toDto(GraphicsCard entity) {
        GraphicsCardItemDto dto = new GraphicsCardItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ItemType.GRAPHICS_CARD);
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setVram(entity.getVram());
        dto.setGddr(entity.getGddr());
        dto.setPowerDraw(entity.getPower_draw());
        return dto;
    }

    public MemoryItemDto toDto(Memory entity) {
        MemoryItemDto dto = new MemoryItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ItemType.MEMORY);
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setCapacity(entity.getCapacity());
        dto.setType(entity.getType());
        dto.setLatency(entity.getLatency());
        dto.setSpeed(entity.getSpeed());
        return dto;
    }

    public MotherboardItemDto toDto(Motherboard entity) {
        MotherboardItemDto dto = new MotherboardItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ItemType.MOTHERBOARD);
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
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
        dto.setComponentType(ItemType.POWER_SUPPLY);
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setMaxPowerWatt(entity.getMaxPowerWatt());
        return dto;
    }

    public StorageItemDto toDto(Storage entity) {
        StorageItemDto dto = new StorageItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ItemType.STORAGE);
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setCapacity(entity.getCapacity());
        return dto;
    }

    public CaseItemDto toDto(Case entity) {
        CaseItemDto dto = new CaseItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ItemType.CASE_PC);
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setFormat(entity.getFormat());
        return dto;
    }
    public CoolerItemDto toDto(Cooler entity) {
        CoolerItemDto dto = new CoolerItemDto();
        dto.setId(entity.getId());
        dto.setComponentType(ItemType.CPU_COOLER);
        dto.setBrand(entity.getItem().getBrand());
        dto.setCoolerSocketsType(entity.getSocketTypes());
        return dto;
    }


}
