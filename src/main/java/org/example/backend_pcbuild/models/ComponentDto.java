package org.example.backend_pcbuild.models;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;

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
    private String coolerSocketType;

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

//    Power supply
    private Integer powerSupplyMaxPowerWatt;

//    storage
    private Double storageCapacity;
//    case
    private String caseFormat;


}
