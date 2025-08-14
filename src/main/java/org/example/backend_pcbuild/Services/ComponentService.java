package org.example.backend_pcbuild.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
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
    private final OfferRepository offerRepository;

    private final RestClient restClient = RestClient.create();



    public Map<String, List<Object>> fetchComponentsAsMap() {
        return restClient.get()
                .uri("http://127.0.0.1:5000/installComponents")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, List<Object>> fetchOffersAsMap() {
        return restClient.get()
                .uri("http://127.0.0.1:5000/offers")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }


    @Transactional
    public void saveBasedComponents(Map<String, List<Object>> components) {

        for(Map.Entry<String, List<Object>> entry : components.entrySet()) {

            for (Object object : entry.getValue()) {
                try {
                    Map<String, Object> processorData = (Map<String, Object>) object;

                    System.out.println(processorData.toString());
                    Item item = new Item();
                    item.setBrand((String) processorData.get("brand"));
                    item.setModel((String) processorData.get("model"));

                    if (entry.getKey().equalsIgnoreCase("processor")) {
                        Processor processor = new Processor();

                        processor.setCores(getIntegerValue(processorData, "cores"));
                        processor.setThreads(getIntegerValue(processorData, "threads"));
                        processor.setSocket_type(getStringValue(processorData, "socket"));
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
                        motherboard.setSocketType(getStringValue(processorData, "socket_motherboard"));
                        motherboard.setRamSlots(getIntegerValue(processorData, "ramslots"));
                        motherboard.setMemoryType(getStringValue(processorData, "memory_type"));
                        motherboard.setRamCapacity(getIntegerValue(processorData, "memory_capacity"));

                        motherboard.setItem(item);
                        item.setMotherboard(motherboard);

                        motherboardRepository.save(motherboard);

                    } else if (entry.getKey().equalsIgnoreCase("power_supply")) {
                        PowerSupply powerSupply = new PowerSupply();

                        powerSupply.setMaxPowerWatt(getIntegerValue(processorData, "maxPowerWatt"));

                        powerSupply.setItem(item);
                        item.setPowerSupply(powerSupply);

                        powerSupplyRepository.save(powerSupply);
                    } else if (entry.getKey().equalsIgnoreCase("cpu_cooler")) {
                        Cooler cooler = new Cooler();


                        Object socketsObj = processorData.get("sockets");
                        if (socketsObj instanceof List) {
                            List<String> sockets = (List<String>) socketsObj;
                            cooler.setSocketTypes(sockets);
                        }

                        cooler.setItem(item);
                        item.setCooler(cooler);
                        coolerRepository.save(cooler);

                    } else if (entry.getKey().equalsIgnoreCase("graphics_card")) {
                        GraphicsCard graphicsCard = new GraphicsCard();

                        graphicsCard.setGddr(getStringValue(processorData, "gddr"));
                        graphicsCard.setPower_draw(getDoubleValue(processorData, "power_draw"));
                        graphicsCard.setVram(getIntegerValue(processorData, "vram"));

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


    public List<ComponentDto> getAllComponents() {
        List<ComponentDto> result = new ArrayList<>();

        for (GraphicsCard gc : graphicsCardRepository.findAll()) {
            for( Offer offer: gc.getItem().getOffers()){
            result.add(ComponentDto.builder()
                    .componentType("graphicsCard")
                    .brand(gc.getItem().getBrand())
                    .model(gc.getItem().getModel())
                    .condition(offer.getCondition())
                    .photo_url(offer.getPhoto_url())
                    .website_url(offer.getWebsite_url())
                    .price(offer.getPrice())
                    .shop(offer.getShop())
                    .gpuMemorySize(gc.getVram())
                    .gpuGddr(gc.getGddr())
                    .gpuPowerDraw(gc.getPower_draw())
                    .build());
            }
        }
        for (Processor item : processorRepository.findAll()) {
            for( Offer offer: item.getItem().getOffers()) {

                result.add(ComponentDto.builder()
                        .componentType("processor")
                        .brand(item.getItem().getBrand())
                        .model(item.getItem().getModel())
                        .condition(offer.getCondition())
                        .photo_url(offer.getPhoto_url())
                        .website_url(offer.getWebsite_url())
                        .price(offer.getPrice())
                        .shop(offer.getShop())
                        .cpuSocketType(item.getSocket_type())
                        .cpuBase_clock(item.getBase_clock())
                        .cpuCores(item.getCores())
                        .cpuThreads(item.getThreads())
                        .build());
            }
        }
        for (Cooler item : coolerRepository.findAll()) {
            for( Offer offer: item.getItem().getOffers()) {

                result.add(ComponentDto.builder()
                        .componentType("cooler")
                        .brand(item.getItem().getBrand())
                        .model(item.getItem().getModel())
                        .condition(offer.getCondition())
                        .photo_url(offer.getPhoto_url())
                        .website_url(offer.getWebsite_url())
                        .price(offer.getPrice())
                        .shop(offer.getShop())
//                    .coolerSocketType(item.getSocketType())
                        .build());
            }
        }
        for (Memory item : memoryRepository.findAll()) {
            for( Offer offer: item.getItem().getOffers()) {

                result.add(ComponentDto.builder()
                        .componentType("memory")
                        .brand(item.getItem().getBrand())
                        .model(item.getItem().getModel())
                        .condition(offer.getCondition())
                        .photo_url(offer.getPhoto_url())
                        .website_url(offer.getWebsite_url())
                        .price(offer.getPrice())
                        .shop(offer.getShop())
                        .ramCapacity(item.getCapacity())
                        .ramLatency(item.getLatency())
                        .ramSpeed(item.getSpeed())
                        .ramType(item.getType())
                        .build());
            }
        }
        for (Motherboard item : motherboardRepository.findAll()) {
            for( Offer offer: item.getItem().getOffers()) {

                result.add(ComponentDto.builder()
                        .componentType("motherboard")
                        .brand(item.getItem().getBrand())
                        .model(item.getItem().getModel())
                        .condition(offer.getCondition())
                        .photo_url(offer.getPhoto_url())
                        .website_url(offer.getWebsite_url())
                        .price(offer.getPrice())
                        .shop(offer.getShop())
                        .boardChipset(item.getChipset())
                        .boardFormat(item.getFormat())
                        .boardMemoryType(item.getMemoryType())
                        .boardSocketType(item.getSocketType())
                        .boardRamCapacity(item.getRamCapacity())
                        .boardRamSlots(item.getRamSlots())
                        .build());
            }
        }
        for (PowerSupply item : powerSupplyRepository.findAll()) {
            for( Offer offer: item.getItem().getOffers()) {

                result.add(ComponentDto.builder()
                        .componentType("powerSupply")
                        .brand(item.getItem().getBrand())
                        .model(item.getItem().getModel())
                        .condition(offer.getCondition())
                        .photo_url(offer.getPhoto_url())
                        .website_url(offer.getWebsite_url())
                        .price(offer.getPrice())
                        .shop(offer.getShop())
                        .powerSupplyMaxPowerWatt(item.getMaxPowerWatt())
                        .build());
            }
        }
        for (Storage item : storageRepository.findAll()) {
            for( Offer offer: item.getItem().getOffers()) {

                result.add(ComponentDto.builder()
                        .componentType("ssd")
                        .brand(item.getItem().getBrand())
                        .model(item.getItem().getModel())
                        .condition(offer.getCondition())
                        .photo_url(offer.getPhoto_url())
                        .website_url(offer.getWebsite_url())
                        .price(offer.getPrice())
                        .shop(offer.getShop())
                        .storageCapacity(item.getCapacity())
                        .build());
            }
        }

        for (Case item : caseRepository.findAll()) {
            for (Offer offer : item.getItem().getOffers()) {

                result.add(ComponentDto.builder()
                        .componentType("casePc")
                        .brand(item.getItem().getBrand())
                        .model(item.getItem().getModel())
                        .condition(offer.getCondition())
                        .photo_url(offer.getPhoto_url())
                        .website_url(offer.getWebsite_url())
                        .price(offer.getPrice())
                        .shop(offer.getShop())
                        .caseFormat(item.getFormat())
                        .build());
            }
        }
            return result;
    }


    @Transactional
    public void saveAllComponents(Map<String, List<Object>> components) {
        List<Item> itemList = itemRepository.findAll();
        for(Map.Entry<String, List<Object>> entry : components.entrySet()) {

                for (Object object : entry.getValue()) {
                    try {
                        Map<String, Object> processorData = (Map<String, Object>) object;

                        Offer offer = new Offer();
//
                        String statusString = (String) processorData.get("status");
                        ItemCondition condition = ItemCondition.valueOf(statusString);

                        offer.setCondition(condition);
//                        offer.setPrice((Double) processorData.get("price"));
                        Object priceObject = processorData.get("price");
                        if (priceObject instanceof Number) {
                            offer.setPrice(((Number) priceObject).doubleValue());
                        } else {
                            throw new IllegalArgumentException("Invalid price value: " + priceObject);
                        }
                        offer.setShop((String) processorData.get("shop"));
                        offer.setPhoto_url((String) processorData.get("img"));
                        offer.setWebsite_url((String) processorData.get("url"));

                        String offerBrand = (String) processorData.get("brand");
                        String offerModel = (String) processorData.get("model");
                        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

                        Item bestItem = null;
                        double bestScore = 0.0;
                        for (Item item : itemList) {
                            double score = 0.0;
                            if (offerBrand != null && item.getBrand() != null &&
                                    offerBrand.equalsIgnoreCase(item.getBrand())
                            ) {
                                score += 0.5;
                            }
                            if (offerModel != null && item.getModel() != null) {
                                score += similarity.apply(offerModel.toLowerCase(), item.getModel().toLowerCase());
                            }
                            // Dodaj inne parametry jeśli chcesz

                            if (score > bestScore) {
                                bestScore = score;
                                bestItem = item;
                            }
                        }
                        // Próg dopasowania, np. 0.7
                        if (bestItem != null && bestScore > 0.7) {
                            offer.setItem(bestItem);
                            bestItem.getOffers().add(offer);

                            itemRepository.save(bestItem);
                        }

//                        if (entry.getKey().equalsIgnoreCase("processor")) {
//                            Processor processor = new Processor();
//
//                            processor.setCores(getIntegerValue(processorData, "cores"));
//                            processor.setThreads(getIntegerValue(processorData, "threads"));
//                            processor.setSocket_type(getStringValue(processorData, "socket_type"));
//                            processor.setBase_clock(getStringValue(processorData, "base_clock"));
//
//                            processor.setItem(item);
//                            item.setProcessor(processor);
//
//                            processorRepository.save(processor);
//                        } else if (entry.getKey().equalsIgnoreCase("storage")) {
//                            Storage storage = new Storage();
//
//                            storage.setCapacity(getDoubleValue(processorData, "capacity"));
//                            storage.setItem(item);
//                            item.setStorage(storage);
//                            storageRepository.save(storage);
//
//                        } else if (entry.getKey().equalsIgnoreCase("motherboard")) {
//                            Motherboard motherboard = new Motherboard();
//
//                            motherboard.setChipset(getStringValue(processorData, "chipset"));
//                            motherboard.setFormat(getStringValue(processorData, "format"));
//                            motherboard.setMemoryType(getStringValue(processorData, "memory_type"));
//                            motherboard.setSocketType(getStringValue(processorData, "socket_type"));
//
//                            motherboard.setItem(item);
//                            item.setMotherboard(motherboard);
//
//                            motherboardRepository.save(motherboard);
//
//                        } else if (entry.getKey().equalsIgnoreCase("power_supply")) {
//                            PowerSupply powerSupply = new PowerSupply();
//
//                            powerSupply.setMaxPowerWatt(getIntegerValue(processorData, "capacity"));
//
//                            powerSupply.setItem(item);
//                            item.setPowerSupply(powerSupply);
//
//                            powerSupplyRepository.save(powerSupply);
//                        } else if (entry.getKey().equalsIgnoreCase("cooler")) {
//                            Cooler cooler = new Cooler();
//
//                            cooler.setSocketTypes(getStringValue(processorData, "socket_type"));
//
//                            cooler.setItem(item);
//                            item.setCooler(cooler);
//                            coolerRepository.save(cooler);
//
//                        } else if (entry.getKey().equalsIgnoreCase("graphics_card")) {
//                            GraphicsCard graphicsCard = new GraphicsCard();
//
//                            graphicsCard.setGddr(getStringValue(processorData, "gddr"));
//                            graphicsCard.setPower_draw(getDoubleValue(processorData, "power_draw"));
//                            graphicsCard.setVram(getIntegerValue(processorData, "memory_size"));
//
//                            graphicsCard.setItem(item);
//                            item.setGraphicsCard(graphicsCard);
//
//                            graphicsCardRepository.save(graphicsCard);
//
//                        } else if (entry.getKey().equalsIgnoreCase("case")) {
//                            Case case1 = new Case();
//
//                            case1.setFormat(getStringValue(processorData, "format"));
//
//                            case1.setItem(item);
//                            item.setCase_(case1);
//                            caseRepository.save(case1);
//                        } else if (entry.getKey().equalsIgnoreCase("ram")) {
//                            Memory memory = new Memory();
//
//                            memory.setCapacity(getIntegerValue(processorData, "capacity"));
//                            memory.setType(getStringValue(processorData, "type"));
//                            memory.setSpeed(getStringValue(processorData, "speed"));
//                            memory.setLatency(getStringValue(processorData, "latency"));
//
//                            memory.setItem(item);
//                            item.setMemory(memory);
//                            memoryRepository.save(memory);
//                        }


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
