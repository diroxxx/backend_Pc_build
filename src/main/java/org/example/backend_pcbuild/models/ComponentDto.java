package org.example.backend_pcbuild.models;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ComponentDto {
//    item
    private String brand;
    private String model;
    private ItemCondition condition;
    @Column(length = 1000)
    private String photo_url;
    @Column(length = 1000) // Zwiększ z domyślnych 255 na 1000
    private String website_url;
    private Double price;
    private String shop;

    private String componentType;

//  processor
    private Integer cpuCores;
    private Integer cpuThreads;
    private String cpuSocketType;
    private String cpuBase_clock;

//    Cooler
    private List<String> coolerSocketsType;

//    GraphicsCard
    private Integer gpuMemorySize;
    private String gpuGddr;
    private Double gpuPowerDraw;

//    memory
    private String ramType;
    private Integer ramCapacity;
    private String ramSpeed;
    private String ramLatency;

//    motherboard
    private String boardChipset;
    private String boardSocketType;
    private String boardMemoryType;
    private String boardFormat;
    private Integer boardRamSlots;
    private Integer boardRamCapacity;

//    Power supply
    private Integer powerSupplyMaxPowerWatt;

//    storage
    private Double storageCapacity;
//    case
    private String caseFormat;


    // Mapowanie dla GraphicsCard
    public static ComponentDto fromGraphicsCard(GraphicsCard gc, Offer offer) {
        return ComponentDto.builder()
                .componentType("graphicsCard")
                .brand(gc.getItem().getBrand())
                .model(gc.getItem().getModel())
                .condition(offer.getCondition())
                .photo_url(offer.getPhotoUrl())
                .website_url(offer.getWebsiteUrl())
                .price(offer.getPrice())
                .shop(offer.getShop())
                .gpuMemorySize(gc.getVram())
                .gpuGddr(gc.getGddr())
                .gpuPowerDraw(gc.getPower_draw())
                .build();
    }

    // Mapowanie dla Processor
    public static ComponentDto fromProcessor(Processor processor, Offer offer) {
        return ComponentDto.builder()
                .componentType("processor")
                .brand(processor.getItem().getBrand())
                .model(processor.getItem().getModel())
                .condition(offer.getCondition())
                .photo_url(offer.getPhotoUrl())
                .website_url(offer.getWebsiteUrl())
                .price(offer.getPrice())
                .shop(offer.getShop())
                .cpuSocketType(processor.getSocket_type())
                .cpuBase_clock(processor.getBase_clock())
                .cpuCores(processor.getCores())
                .cpuThreads(processor.getThreads())
                .build();
    }
    public static ComponentDto fromCooler(Cooler cooler, Offer offer) {
        return ComponentDto.builder()
                .componentType("cooler")
                .brand(cooler.getItem().getBrand())
                .model(cooler.getItem().getModel())
                .condition(offer.getCondition())
                .photo_url(offer.getPhotoUrl())
                .website_url(offer.getWebsiteUrl())
                .price(offer.getPrice())
                .shop(offer.getShop())
                .build();
    }

    public static ComponentDto fromMemory(Memory memory, Offer offer) {
        return ComponentDto.builder()
                .componentType("memory")
                .brand(memory.getItem().getBrand())
                .model(memory.getItem().getModel())
                .condition(offer.getCondition())
                .photo_url(offer.getPhotoUrl())
                .website_url(offer.getWebsiteUrl())
                .price(offer.getPrice())
                .shop(offer.getShop())
                .ramCapacity(memory.getCapacity())
                .ramLatency(memory.getLatency())
                .ramSpeed(memory.getSpeed())
                .ramType(memory.getType())
                .build();
    }

    public static ComponentDto fromMotherboard(Motherboard motherboard, Offer offer) {
        return ComponentDto.builder()
                .componentType("motherboard")
                .brand(motherboard.getItem().getBrand())
                .model(motherboard.getItem().getModel())
                .condition(offer.getCondition())
                .photo_url(offer.getPhotoUrl())
                .website_url(offer.getWebsiteUrl())
                .price(offer.getPrice())
                .shop(offer.getShop())
                .boardChipset(motherboard.getChipset())
                .boardFormat(motherboard.getFormat())
                .boardMemoryType(motherboard.getMemoryType())
                .boardSocketType(motherboard.getSocketType())
                .boardRamCapacity(motherboard.getRamCapacity())
                .boardRamSlots(motherboard.getRamSlots())
                .build();
    }

    public static ComponentDto fromPowerSupply(PowerSupply powerSupply, Offer offer) {
        return ComponentDto.builder()
                .componentType("powerSupply")
                .brand(powerSupply.getItem().getBrand())
                .model(powerSupply.getItem().getModel())
                .condition(offer.getCondition())
                .photo_url(offer.getPhotoUrl())
                .website_url(offer.getWebsiteUrl())
                .price(offer.getPrice())
                .shop(offer.getShop())
                .powerSupplyMaxPowerWatt(powerSupply.getMaxPowerWatt())
                .build();
    }

    public static ComponentDto fromStorage(Storage storage, Offer offer) {
        return ComponentDto.builder()
                .componentType("ssd")
                .brand(storage.getItem().getBrand())
                .model(storage.getItem().getModel())
                .condition(offer.getCondition())
                .photo_url(offer.getPhotoUrl())
                .website_url(offer.getWebsiteUrl())
                .price(offer.getPrice())
                .shop(offer.getShop())
                .storageCapacity(storage.getCapacity())
                .build();
    }

    public static ComponentDto fromCase(Case caseItem, Offer offer) {
        return ComponentDto.builder()
                .componentType("casePc")
                .brand(caseItem.getItem().getBrand())
                .model(caseItem.getItem().getModel())
                .condition(offer.getCondition())
                .photo_url(offer.getPhotoUrl())
                .website_url(offer.getWebsiteUrl())
                .price(offer.getPrice())
                .shop(offer.getShop())
                .caseFormat(caseItem.getFormat())
                .build();
    }


}
