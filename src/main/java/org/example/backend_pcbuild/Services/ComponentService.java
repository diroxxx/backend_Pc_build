package org.example.backend_pcbuild.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ComponentService {

    private final PowerSupplyRepository powerSupplyRepository;
    private final CaseRepository caseRepository;
    private final CoolerRepository coolerRepository;
    private final GraphicsCardRepository graphicsCardRepository;
    private final ItemRepository itemRepository;
    private final MemoryRepository memoryRepository;
    private final MotherboardRepository motherboardRepository;
    private final ProcessorRepository processorRepository;
    private final StorageRepository storageRepository;

    private final RestClient restClient = RestClient.create();



    public Map<String, List<Object>> fetchComponentsAsMap() {
        return restClient.get()
                .uri("http://127.0.0.1:5000/components")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});


    }
    public List<ComponentDto> getAllComponents() {
        List<ComponentDto> result = new ArrayList<>();

        for (GraphicsCard gc : graphicsCardRepository.findAll()) {
            result.add(ComponentDto.builder()
                    .componentType("graphicsCard")
                    .brand(gc.getItem().getBrand())
                    .model(gc.getItem().getModel())
                    .condition(gc.getItem().getCondition())
                    .photo_url(gc.getItem().getPhoto_url())
                    .website_url(gc.getItem().getWebsite_url())
                    .price(gc.getItem().getPrice())
                    .shop(gc.getItem().getShop())
                    .gpuMemorySize(gc.getMemorySize())
                    .gpuGddr(gc.getGddr())
                    .gpuPowerDraw(gc.getPower_draw())
                    .build());
        }
        for (Processor item : processorRepository.findAll()) {
            result.add(ComponentDto.builder()
                    .componentType("graphicsCard")
                    .brand(item.getItem().getBrand())
                    .model(item.getItem().getModel())
                    .condition(item.getItem().getCondition())
                    .photo_url(item.getItem().getPhoto_url())
                    .website_url(item.getItem().getWebsite_url())
                    .price(item.getItem().getPrice())
                    .shop(item.getItem().getShop())
                    .cpuSocketType(item.getSocket_type())
                    .cpuBase_clock(item.getBase_clock())
                    .cpuCores(item.getCores())
                    .cpuThreads(item.getThreads())
                    .build());
        }
        for (Cooler item : coolerRepository.findAll()) {
            result.add(ComponentDto.builder()
                    .componentType("graphicsCard")
                    .brand(item.getItem().getBrand())
                    .model(item.getItem().getModel())
                    .condition(item.getItem().getCondition())
                    .photo_url(item.getItem().getPhoto_url())
                    .website_url(item.getItem().getWebsite_url())
                    .price(item.getItem().getPrice())
                    .shop(item.getItem().getShop())
                    .coolerSocketType(item.getSocketType())
                    .build());
        }
        for (Memory item : memoryRepository.findAll()) {
            result.add(ComponentDto.builder()
                    .componentType("graphicsCard")
                    .brand(item.getItem().getBrand())
                    .model(item.getItem().getModel())
                    .condition(item.getItem().getCondition())
                    .photo_url(item.getItem().getPhoto_url())
                    .website_url(item.getItem().getWebsite_url())
                    .price(item.getItem().getPrice())
                    .shop(item.getItem().getShop())
                    .ramCapacity(item.getCapacity())
                    .ramLatency(item.getLatency())
                    .ramSpeed(item.getSpeed())
                    .ramType(item.getType())
                    .build());
        }
        for (Motherboard item : motherboardRepository.findAll()) {
            result.add(ComponentDto.builder()
                    .componentType("graphicsCard")
                    .brand(item.getItem().getBrand())
                    .model(item.getItem().getModel())
                    .condition(item.getItem().getCondition())
                    .photo_url(item.getItem().getPhoto_url())
                    .website_url(item.getItem().getWebsite_url())
                    .price(item.getItem().getPrice())
                    .shop(item.getItem().getShop())
                    .boardChipset(item.getChipset())
                            .boardFormat(item.getFormat())
                            .boardMemoryType(item.getMemoryType())
                            .boardSocketType(item.getSocketType())
                    .build());
        }
        for (PowerSupply item : powerSupplyRepository.findAll()) {
            result.add(ComponentDto.builder()
                    .componentType("graphicsCard")
                    .brand(item.getItem().getBrand())
                    .model(item.getItem().getModel())
                    .condition(item.getItem().getCondition())
                    .photo_url(item.getItem().getPhoto_url())
                    .website_url(item.getItem().getWebsite_url())
                    .price(item.getItem().getPrice())
                    .shop(item.getItem().getShop())
                    .powerSupplyMaxPowerWatt(item.getMaxPowerWatt())
                    .build());
        }
        for (Storage item : storageRepository.findAll()) {
            result.add(ComponentDto.builder()
                    .componentType("graphicsCard")
                    .brand(item.getItem().getBrand())
                    .model(item.getItem().getModel())
                    .condition(item.getItem().getCondition())
                    .photo_url(item.getItem().getPhoto_url())
                    .website_url(item.getItem().getWebsite_url())
                    .price(item.getItem().getPrice())
                    .shop(item.getItem().getShop())
                    .storageCapacity(item.getCapacity())
                    .build());
        }

        for (Case item : caseRepository.findAll()) {
            result.add(ComponentDto.builder()
                    .componentType("graphicsCard")
                    .brand(item.getItem().getBrand())
                    .model(item.getItem().getModel())
                    .condition(item.getItem().getCondition())
                    .photo_url(item.getItem().getPhoto_url())
                    .website_url(item.getItem().getWebsite_url())
                    .price(item.getItem().getPrice())
                    .shop(item.getItem().getShop())
                    .caseFormat(item.getFormat())
                    .build());
        }
            return result;
    }


    @Transactional
    public void saveAllComponents(Map<String, List<Object>> components) {

        for(Map.Entry<String, List<Object>> entry : components.entrySet()) {

                for (Object object : entry.getValue()) {
                    try {
                        Map<String, Object> processorData = (Map<String, Object>) object;

                        Item item = new Item();
                        item.setBrand((String) processorData.get("brand"));
                        item.setModel((String) processorData.get("model"));
                        String statusString = (String) processorData.get("status");
                        ItemCondition condition = ItemCondition.valueOf(statusString);

                        item.setCondition(condition);
                        item.setPrice((Double) processorData.get("price"));
//                        System.out.println(processorData.get("price"));

                        item.setShop((String) processorData.get("shop"));
                        item.setPhoto_url((String) processorData.get("img"));
                        item.setWebsite_url((String) processorData.get("url"));

                        if (entry.getKey().equalsIgnoreCase("processor")) {
                            Processor processor = new Processor();

                            processor.setCores(getIntegerValue(processorData, "cores"));
                            processor.setThreads(getIntegerValue(processorData, "threads"));
                            processor.setSocket_type(getStringValue(processorData, "socket_type"));
                            processor.setBase_clock(getStringValue(processorData, "base_clock"));

                            processor.setItem(item);
                            item.setProcessor(processor);

                            processorRepository.save(processor);
                        } else if (entry.getKey().equalsIgnoreCase("storage")) {
                            Storage storage = new Storage();

                            storage.setCapacity(getDoubleValue(processorData, "capacity"));
                            storage.setItem(item);
                            item.setStorage(storage);
                            storageRepository.save(storage);

                        } else if (entry.getKey().equalsIgnoreCase("motherboard")) {
                            Motherboard motherboard = new Motherboard();

                            motherboard.setChipset(getStringValue(processorData, "chipset"));
                            motherboard.setFormat(getStringValue(processorData, "format"));
                            motherboard.setMemoryType(getStringValue(processorData, "memory_type"));
                            motherboard.setSocketType(getStringValue(processorData, "socket_type"));
                            
                            motherboard.setItem(item);
                            item.setMotherboard(motherboard);
                            
                            motherboardRepository.save(motherboard);
                            
                        } else if (entry.getKey().equalsIgnoreCase("power_supply")) {
                            PowerSupply powerSupply = new PowerSupply();

                            powerSupply.setMaxPowerWatt(getIntegerValue(processorData, "capacity"));

                            powerSupply.setItem(item);
                            item.setPowerSupply(powerSupply);

                            powerSupplyRepository.save(powerSupply);
                        } else if (entry.getKey().equalsIgnoreCase("cooler")) {
                            Cooler cooler = new Cooler();

                            cooler.setSocketType(getStringValue(processorData, "socket_type"));

                            cooler.setItem(item);
                            item.setCooler(cooler);
                            coolerRepository.save(cooler);

                        } else if (entry.getKey().equalsIgnoreCase("graphics_card")) {
                            GraphicsCard graphicsCard = new GraphicsCard();

                            graphicsCard.setGddr(getStringValue(processorData, "gddr"));
                            graphicsCard.setPower_draw(getDoubleValue(processorData, "power_draw"));
                            graphicsCard.setMemorySize(getIntegerValue(processorData, "memory_size"));

                            graphicsCard.setItem(item);
                            item.setGraphicsCard(graphicsCard);

                            graphicsCardRepository.save(graphicsCard);

                        } else if (entry.getKey().equalsIgnoreCase("case")) {
                            Case case1 = new Case();

                            case1.setFormat(getStringValue(processorData, "format"));
                            
                            case1.setItem(item);
                            item.setCase_(case1);
                            caseRepository.save(case1);
                        } else if (entry.getKey().equalsIgnoreCase("ram")) {
                            Memory memory = new Memory();

                            memory.setCapacity(getIntegerValue(processorData, "capacity"));
                            memory.setType(getStringValue(processorData, "type"));
                            memory.setSpeed(getStringValue(processorData, "speed"));
                            memory.setLatency(getStringValue(processorData, "latency"));

                            memory.setItem(item);
                            item.setMemory(memory);
                            memoryRepository.save(memory);
                        }


                    } catch (ClassCastException e) {
                        System.err.println(e.getMessage());
                        throw e;

                    } catch (Exception e) {
                        System.err.println( e.getMessage());
                        throw e;

                    }
            }
        }
    }

    // Pomocnicze metody do bezpiecznego pobierania wartości
    private Integer getIntegerValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    private Double getDoubleValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }



}
