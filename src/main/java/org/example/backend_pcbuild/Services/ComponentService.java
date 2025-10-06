package org.example.backend_pcbuild.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
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
    private final OfferMatchingService offerMatchingService;

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


                    Item item = new Item();
                    item.setBrand((String) processorData.get("brand"));
                    item.setModel((String) processorData.get("model"));
                    Item itemToCheck = itemRepository.findByBrandAndModel((String) processorData.get("brand"), (String) processorData.get("model")).orElse(null);

                    if (itemToCheck != null) {
                        continue;
                    }

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
                    .photo_url(offer.getPhotoUrl())
                    .website_url(offer.getWebsiteUrl())
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
                        .photo_url(offer.getPhotoUrl())
                        .website_url(offer.getWebsiteUrl())
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
                        .photo_url(offer.getPhotoUrl())
                        .website_url(offer.getWebsiteUrl())
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
                        .photo_url(offer.getPhotoUrl())
                        .website_url(offer.getWebsiteUrl())
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
                        .photo_url(offer.getPhotoUrl())
                        .website_url(offer.getWebsiteUrl())
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
                        .photo_url(offer.getPhotoUrl())
                        .website_url(offer.getWebsiteUrl())
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
                        .photo_url(offer.getPhotoUrl())
                        .website_url(offer.getWebsiteUrl())
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
                        .photo_url(offer.getPhotoUrl())
                        .website_url(offer.getWebsiteUrl())
                        .price(offer.getPrice())
                        .shop(offer.getShop())
                        .caseFormat(item.getFormat())
                        .build());
            }
        }
            return result;
    }

    private static final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

    @Transactional
    public void saveAllOffers(Map<String, List<Object>> components) {
        List<GraphicsCard> graphicsCardList = graphicsCardRepository.findAll();
        List<Processor> processorList = processorRepository.findAll();
        List<Memory> memoryList = memoryRepository.findAll();
        List<Motherboard> motherboardList = motherboardRepository.findAll();
        List<PowerSupply> powerSupplyList = powerSupplyRepository.findAll();
        List<Storage> storageList = storageRepository.findAll();
        List<Case> caseList = caseRepository.findAll();
        List<Cooler> coolerList = coolerRepository.findAll();

        Map<String, List<?>> categoryMap = Map.of(
                "graphics_card", graphicsCardList,
                "processor", processorList,
                "ram", memoryList,
                "motherboard", motherboardList,
                "power_supply", powerSupplyList,
                "storage", storageList,
                "case", caseList,
                "cpu_cooler", coolerList
        );

        for (Map.Entry<String, List<Object>> entry : components.entrySet()) {
            String offerCategory = entry.getKey().toLowerCase();
            List<?> itemsForCategory = categoryMap.getOrDefault(offerCategory, Collections.emptyList());

            for (Object object : entry.getValue()) {
                try {
                    Map<String, Object> componentData = (Map<String, Object>) object;
                    Offer offer = buildOfferFromData(componentData);
                    String url = offer.getWebsiteUrl();

                    if (offer.getCondition() == null || offer.getShop() == null || offer.getPhotoUrl() == null || url == null) {
                        continue;
                    }

                    Optional<Offer> existedOffer = offerRepository.findByWebsiteUrl(url);
                    if (existedOffer.isPresent()) {
                        // Tu można dodać obsługę aktualizacji oferty, jeśli jest nieaktualna
                        continue;
                    }

                    Item bestItem = offerMatchingService.matchOfferToItem(offerCategory, componentData, itemsForCategory);
                    if (bestItem != null) {
                        offer.setItem(bestItem);
                        bestItem.getOffers().add(offer);
                        itemRepository.save(bestItem);
                    }
                } catch (Exception e) {
                    System.err.println("[saveAllOffers] " + e.getMessage());
                }
            }
        }
    }

    private Offer buildOfferFromData(Map<String, Object> componentData) {
        Offer offer = new Offer();

        String statusString = (String) componentData.get("status");
        ItemCondition condition = null;
        if (statusString != null) {
            try {
                condition = ItemCondition.valueOf(statusString);
            } catch (IllegalArgumentException ignored) {}
        }
        offer.setCondition(condition);

        Object priceObject = componentData.get("price");
        offer.setPrice(parsePrice(priceObject));

        String shop = (String) componentData.get("shop");
        String img = (String) componentData.get("img");
        String url = (String) componentData.get("url");
        offer.setShop(shop);
        offer.setPhotoUrl(img);
        offer.setWebsiteUrl(url);

        return offer;
    }

    private Double parsePrice(Object priceObject) {
        if (priceObject instanceof Number) {
            return ((Number) priceObject).doubleValue();
        } else if (priceObject instanceof String) {
            String p = ((String) priceObject).replaceAll("[^\\d,\\.]", "").replace(",", ".");
            if (!p.isEmpty()) {
                try {
                    return Double.parseDouble(p);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid price value: " + priceObject);
                }
            } else {
                throw new IllegalArgumentException("Invalid price value: " + priceObject);
            }
        } else {
            throw new IllegalArgumentException("Invalid price value: " + priceObject);
        }
    }

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
