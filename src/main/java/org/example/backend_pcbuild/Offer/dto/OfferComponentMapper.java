package org.example.backend_pcbuild.Offer.dto;

import org.example.backend_pcbuild.models.*;
import org.springframework.stereotype.Component;

@Component
public class OfferComponentMapper {

    public static ProcessorDto toDto(Processor entity, Offer offer) {
        ProcessorDto dto = new ProcessorDto();
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShop(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setSocketType(entity.getSocket_type());
        dto.setBaseClock(entity.getBase_clock());
        dto.setCores(entity.getCores());
        dto.setThreads(entity.getThreads());
        return dto;
    }

    public static GraphicsCardDto toDto(GraphicsCard entity, Offer offer) {
        GraphicsCardDto dto = new GraphicsCardDto();
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShop(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setVram(entity.getVram());
        dto.setGddr(entity.getGddr());
        dto.setPowerDraw(entity.getPower_draw());
        return dto;
    }

    public static MemoryDto toDto(Memory entity, Offer offer) {
        MemoryDto dto = new MemoryDto();
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShop(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setCapacity(entity.getCapacity());
        dto.setType(entity.getType());
        dto.setLatency(entity.getLatency());
        dto.setSpeed(entity.getSpeed());
        return dto;
    }

    public static MotherboardDto toDto(Motherboard entity, Offer offer) {
        MotherboardDto dto = new MotherboardDto();
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShop(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setChipset(entity.getChipset());
        dto.setFormat(entity.getFormat());
        dto.setMemoryType(entity.getMemoryType());
        dto.setSocketType(entity.getSocketType());
        dto.setRamCapacity(entity.getRamCapacity());
        dto.setRamSlots(entity.getRamSlots());
        return dto;
    }

    public static PowerSupplyDto toDto(PowerSupply entity, Offer offer) {
        PowerSupplyDto dto = new PowerSupplyDto();
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShop(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setMaxPowerWatt(entity.getMaxPowerWatt());
        return dto;
    }

    public static StorageDto toDto(Storage entity, Offer offer) {
        StorageDto dto = new StorageDto();
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShop(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setCapacity(entity.getCapacity());
        return dto;
    }

    public static CaseDto toDto(Case entity, Offer offer) {
        CaseDto dto = new CaseDto();
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShop(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setFormat(entity.getFormat());
        return dto;
    }

    public static CoolerDto toDto(Cooler entity, Offer offer) {
        CoolerDto dto = new CoolerDto();
        dto.setBrand(entity.getItem().getBrand());
        dto.setModel(entity.getItem().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShop(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setCoolerSocketsType(entity.getSocketTypes());
        return dto;
    }

}
