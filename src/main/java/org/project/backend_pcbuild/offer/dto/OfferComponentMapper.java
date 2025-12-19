package org.project.backend_pcbuild.offer.dto;

import org.project.backend_pcbuild.offer.model.Offer;
import org.project.backend_pcbuild.pcComponents.model.*;
import org.project.backend_pcbuild.pcComponents.model.*;
import org.springframework.stereotype.Component;

@Component
public class OfferComponentMapper {

    public static ProcessorDto toDto(Processor entity, Offer offer) {
        ProcessorDto dto = new ProcessorDto();
        dto.setId(entity.getId());
        dto.setTitle(offer.getTitle());
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShopName(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setSocketType(entity.getSocketType());
        dto.setBaseClock(entity.getBaseClock());
        dto.setCores(entity.getCores());
        dto.setThreads(entity.getThreads());
        dto.setBaseClock(entity.getBaseClock());
        dto.setTdp(entity.getTdp());
        dto.setBoostClock(entity.getBoostClock());
        dto.setIntegratedGraphics(entity.getIntegratedGraphics());
        return dto;
    }

    public static GraphicsCardDto toDto(GraphicsCard entity, Offer offer) {
        GraphicsCardDto dto = new GraphicsCardDto();
        dto.setId(entity.getId());
        dto.setTitle(offer.getTitle());
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShopName(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setVram(entity.getVram());
        dto.setGddr(entity.getGddr());
        dto.setPowerDraw(entity.getPowerDraw());
        dto.setBoostClock(entity.getBoostClock());
        dto.setCoreClock(entity.getCoreClock());
        dto.setLengthInMM(entity.getLengthInMM());

        return dto;
    }

    public static MemoryDto toDto(Memory entity, Offer offer) {
        MemoryDto dto = new MemoryDto();
        dto.setId(entity.getId());
        dto.setTitle(offer.getTitle());
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShopName(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setCapacity(entity.getCapacity());
        dto.setType(entity.getType());
        dto.setLatency(entity.getLatency());
        dto.setSpeed(entity.getSpeed());
        dto.setAmount(entity.getAmount());

        return dto;
    }

    public static MotherboardDto toDto(Motherboard entity, Offer offer) {
        MotherboardDto dto = new MotherboardDto();
        dto.setId(entity.getId());
        dto.setTitle(offer.getTitle());
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShopName(offer.getShop() != null ? offer.getShop().getName() : null);

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
        dto.setId(entity.getId());
        dto.setTitle(offer.getTitle());
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShopName(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setMaxPowerWatt(entity.getMaxPowerWatt());
        dto.setType(entity.getType());
        dto.setEfficiencyRating(entity.getEfficiencyRating());
        dto.setModular(entity.getModular());
        return dto;
    }

    public static StorageDto toDto(Storage entity, Offer offer) {
        StorageDto dto = new StorageDto();
        dto.setId(entity.getId());
        dto.setTitle(offer.getTitle());
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShopName(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setCapacity(entity.getCapacity());
        return dto;
    }

    public static CaseDto toDto(Case entity, Offer offer) {
        CaseDto dto = new CaseDto();
        dto.setId(entity.getId());
        dto.setTitle(offer.getTitle());
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShopName(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setFormat(entity.getFormat());
        return dto;
    }

    public static CoolerDto toDto(Cooler entity, Offer offer) {
        CoolerDto dto = new CoolerDto();
        dto.setId(entity.getId());
        dto.setTitle(offer.getTitle());
        dto.setBrand(entity.getComponent().getBrand().getName());
        dto.setModel(entity.getComponent().getModel());
        dto.setCondition(offer.getCondition());
        dto.setPhotoUrl(offer.getPhotoUrl());
        dto.setWebsiteUrl(offer.getWebsiteUrl());
        dto.setPrice(offer.getPrice());
        dto.setShopName(offer.getShop() != null ? offer.getShop().getName() : null);

        dto.setCoolerSocketsType(entity.getSocketTypes());
        dto.setFanRpm(entity.getFanRpm());
        dto.setNoiseLevel(entity.getNoiseLevel());
        dto.setRadiatorSize(entity.getRadiatorSize());
        return dto;
    }

}
