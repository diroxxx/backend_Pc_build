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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    // Progi per kategoria (accept, review)
//    private static final Map<String, double[]> THRESHOLDS = Map.of(
//            "processor", new double[]{0.70, 0.60},
//            "graphics_card", new double[]{0.72, 0.62},
//            "ram", new double[]{0.65, 0.55},
//            "storage", new double[]{0.68, 0.58},
//            "motherboard", new double[]{0.70, 0.60},
//            "power_supply", new double[]{0.65, 0.55},
//            "cooler", new double[]{0.60, 0.50},
//            "case", new double[]{0.60, 0.50}
//    );
//    private static final Pattern SOCKET_RX = Pattern.compile("\\b(am4|am5|lga\\s?1700|lga\\s?1200|lga\\s?1151|lga\\s?1150)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern CPU_MODEL_INTEL = Pattern.compile("\\bi[3579]-?\\d{3,5}[a-z]?\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern CPU_MODEL_RYZEN = Pattern.compile("\\bryzen\\s?[3579]?\\s?\\d{3,4}x?\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern GPU_SERIES = Pattern.compile("\\b(rtx|gtx|rx)\\s?\\d{3,4}\\s?(ti|xt|super)?\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern VRAM_RX = Pattern.compile("(\\d+)\\s?(gb|g)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern SPEED_RX = Pattern.compile("\\b(\\d{3,5})\\s?mhz\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern DDR_RX = Pattern.compile("\\bddr(3|4|5)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern WATT_RX = Pattern.compile("\\b(\\d{3,4})\\s?w\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern CAPACITY_RX = Pattern.compile("(\\d+)(tb|gb)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern GDDR_RX = Pattern.compile("\\bgddr(6x|6|5x|5)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern CHIPSET_RX = Pattern.compile("\\b(b450|b550|x570|z790|b760|h610|z690|b650|x670|z590|z490)\\b", Pattern.CASE_INSENSITIVE);


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

        List<Item> itemList = itemRepository.findAll();
        log.info("Rozpoczęto przypisywanie ofert do podzespołów, liczba kategorii: {}", components.size());
        for(Map.Entry<String, List<Object>> entry : components.entrySet()) {

                for (Object object : entry.getValue()) {
                    try {
                        Map<String, Object> processorData = (Map<String, Object>) object;

                        Offer offer = new Offer();
//
                        String statusString = (String) processorData.get("status");
                        ItemCondition condition = ItemCondition.valueOf(statusString);

                        offer.setCondition(condition);
                        Object priceObject = processorData.get("price");
                        if (priceObject instanceof Number) {
                            offer.setPrice(((Number) priceObject).doubleValue());
                        } else {
                            throw new IllegalArgumentException("Invalid price value: " + priceObject);
                        }
                        String shop = (String) processorData.get("shop");
                        String img = (String) processorData.get("img");
                        String url = (String) processorData.get("url");
                        String offerBrand = (String) processorData.get("brand");
                        String offerModel = (String) processorData.get("model");
                        String offerCategory = (String) processorData.get("category");

                        offer.setShop(shop);
                        offer.setPhotoUrl(img);
                        offer.setWebsiteUrl(url);

                        if (offerCategory == null || offerBrand == null || offerModel == null) {
                            continue;
                        }

                        Optional<Offer> existedOffer = offerRepository.findByWebsiteUrl(url);

                        if (existedOffer.isPresent()) {
                            continue;
                        }

                        log.info("Przetwarzanie oferty: brand={}, model={}, kategoria={}", offerBrand, offerModel, offerCategory);
                        Item bestItem = null;
                        double bestScore = 0.0;
                        if (offerCategory.equalsIgnoreCase("processor")) {
                            for(Processor processor : processorList){
                                double score = 0.0;
                                if (processor.getItem().getBrand() != null &&
                                        offerBrand.equalsIgnoreCase(processor.getItem().getBrand())
                                ) {
                                    score += 0.3;
                                }
                                if (processor.getItem().getModel() != null) {
                                    score += similarity.apply(offerModel.toLowerCase(), processor.getItem().getModel().toLowerCase());
                                }

                                if (score > bestScore) {
                                    bestScore = score;
                                    bestItem = processor.getItem();
                                    log.info("Przypisano ofertę {},  do podzespołu: {}, dopasowanie punktowe: {}", offerModel, bestItem.getModel(), bestScore);
                                } else {
                                    log.debug("Oferta nie została przypisana z powodu zbyt niskiego wyniku dopasowania: {}. Oferta: model={}, brand={}",
                                            bestScore, offerModel, offerBrand);
                                }
                            }
                            }  else if (offerCategory.equalsIgnoreCase("graphics_card")) {
                            for(GraphicsCard graphicsCard : graphicsCardList){
                                double score = 0.0;
                                if (graphicsCard.getItem().getBrand() != null &&
                                        offerBrand.equalsIgnoreCase(graphicsCard.getItem().getBrand())
                                ) {
                                    score += 0.3;
                                }
                                if (graphicsCard.getItem().getModel() != null) {
                                    score += similarity.apply(offerModel.toLowerCase(), graphicsCard.getItem().getModel().toLowerCase());
                                }

                                if (score > bestScore) {
                                    bestScore = score;
                                    bestItem = graphicsCard.getItem();
                                    log.info("Przypisano ofertę {},  do podzespołu: {}, dopasowanie punktowe: {}", offerModel, bestItem.getModel(), bestScore);
                                } else {
                                    log.debug("Oferta nie została przypisana z powodu zbyt niskiego wyniku dopasowania: {}. Oferta: model={}, brand={}",
                                            bestScore, offerModel, offerBrand);
                                }
                            }
                        } else if (offerCategory.equalsIgnoreCase("ram")) {
                            for(Memory memory : memoryList){
                                double score = 0.0;
                                if (memory.getItem().getBrand() != null &&
                                        offerBrand.equalsIgnoreCase(memory.getItem().getBrand())
                                ) {
                                    score += 0.3;
                                }
                                if (memory.getItem().getModel() != null) {
                                    score += similarity.apply(offerModel.toLowerCase(), memory.getItem().getModel().toLowerCase());
                                }

                                if (score > bestScore) {
                                    bestScore = score;
                                    bestItem = memory.getItem();
                                    log.info("Przypisano ofertę {},  do podzespołu: {}, dopasowanie punktowe: {}", offerModel, bestItem.getModel(), bestScore);
                                } else {
                                    log.debug("Oferta nie została przypisana z powodu zbyt niskiego wyniku dopasowania: {}. Oferta: model={}, brand={}",
                                            bestScore, offerModel, offerBrand);
                                }
                            }
                        }else if (offerCategory.equalsIgnoreCase("case")) {
                            for(Case casePc : caseList){
                                double score = 0.0;
                                if (casePc.getItem().getBrand() != null &&
                                        offerBrand.equalsIgnoreCase(casePc.getItem().getBrand())
                                ) {
                                    score += 0.3;
                                }
                                if (casePc.getItem().getModel() != null) {
                                    score += similarity.apply(offerModel.toLowerCase(), casePc.getItem().getModel().toLowerCase());
                                }

                                if (score > bestScore) {
                                    bestScore = score;
                                    bestItem = casePc.getItem();
                                    log.info("Przypisano ofertę {},  do podzespołu: {}, dopasowanie punktowe: {}", offerModel, bestItem.getModel(), bestScore);
                                } else {
                                    log.debug("Oferta nie została przypisana z powodu zbyt niskiego wyniku dopasowania: {}. Oferta: model={}, brand={}",
                                            bestScore, offerModel, offerBrand);
                                }
                            }
                        }else if (offerCategory.equalsIgnoreCase("storage")) {
                            for(Storage storage : storageList){
                                double score = 0.0;
                                if (storage.getItem().getBrand() != null &&
                                        offerBrand.equalsIgnoreCase(storage.getItem().getBrand())
                                ) {
                                    score += 0.3;
                                }
                                if (storage.getItem().getModel() != null) {
                                    score += similarity.apply(offerModel.toLowerCase(), storage.getItem().getModel().toLowerCase());
                                }

                                if (score > bestScore) {
                                    bestScore = score;
                                    bestItem = storage.getItem();
                                    log.info("Przypisano ofertę {},  do podzespołu: {}, dopasowanie punktowe: {}", offerModel, bestItem.getModel(), bestScore);
                                } else {
                                    log.debug("Oferta nie została przypisana z powodu zbyt niskiego wyniku dopasowania: {}. Oferta: model={}, brand={}",
                                            bestScore, offerModel, offerBrand);
                                }
                            }
                        }else if (offerCategory.equalsIgnoreCase("power_supply")) {
                            for(PowerSupply powerSupply : powerSupplyList){
                                double score = 0.0;
                                if (powerSupply.getItem().getBrand() != null &&
                                        offerBrand.equalsIgnoreCase(powerSupply.getItem().getBrand())
                                ) {
                                    score += 0.3;
                                }
                                if (powerSupply.getItem().getModel() != null) {
                                    score += similarity.apply(offerModel.toLowerCase(), powerSupply.getItem().getModel().toLowerCase());
                                }

                                if (score > bestScore) {
                                    bestScore = score;
                                    bestItem = powerSupply.getItem();
                                    log.info("Przypisano ofertę {},  do podzespołu: {}, dopasowanie punktowe: {}", offerModel, bestItem.getModel(), bestScore);
                                } else {
                                    log.debug("Oferta nie została przypisana z powodu zbyt niskiego wyniku dopasowania: {}. Oferta: model={}, brand={}",
                                            bestScore, offerModel, offerBrand);
                                }
                            }
                        }else if (offerCategory.equalsIgnoreCase("motherboard")) {
                            for(Motherboard motherboard : motherboardList){
                                double score = 0.0;
                                if (motherboard.getItem().getBrand() != null &&
                                        offerBrand.equalsIgnoreCase(motherboard.getItem().getBrand())
                                ) {
                                    score += 0.3;
                                }
                                if (motherboard.getItem().getModel() != null) {
                                    score += similarity.apply(offerModel.toLowerCase(), motherboard.getItem().getModel().toLowerCase());
                                }

                                if (score > bestScore) {
                                    bestScore = score;
                                    bestItem = motherboard.getItem();
                                    log.info("Przypisano ofertę {},  do podzespołu: {}, dopasowanie punktowe: {}", offerModel, bestItem.getModel(), bestScore);
                                } else {
                                    log.debug("Oferta nie została przypisana z powodu zbyt niskiego wyniku dopasowania: {}. Oferta: model={}, brand={}",
                                            bestScore, offerModel, offerBrand);
                                }
                            }
                        }else if (offerCategory.equalsIgnoreCase("cpu_cooler")) {
                            for(Cooler cooler : coolerList){
                                double score = 0.0;
                                if (cooler.getItem().getBrand() != null &&
                                        offerBrand.equalsIgnoreCase(cooler.getItem().getBrand())
                                ) {
                                    score += 0.3;
                                }
                                if (cooler.getItem().getModel() != null) {
                                    score += similarity.apply(offerModel.toLowerCase(), cooler.getItem().getModel().toLowerCase());
                                }

                                if (score > bestScore) {
                                    bestScore = score;
                                    bestItem = cooler.getItem();
                                    log.info("Przypisano ofertę {},  do podzespołu: {}, dopasowanie punktowe: {}", offerModel, bestItem.getModel(), bestScore);
                                } else {
                                    log.debug("Oferta nie została przypisana z powodu zbyt niskiego wyniku dopasowania: {}. Oferta: model={}, brand={}",
                                            bestScore, offerModel, offerBrand);
                                }
                            }
                        }

                        if (bestItem != null) {
                            offer.setItem(bestItem);
                            bestItem.getOffers().add(offer);

                            itemRepository.save(bestItem);
                        }
//                        for (Item item : itemList) {
//                            double score = 0.0;
//                            if (offerBrand != null && item.getBrand() != null &&
//                                    offerBrand.equalsIgnoreCase(item.getBrand())
//                            ) {
//                                score += 0.3;
//                            }
//                            if (offerModel != null && item.getModel() != null) {
//                                score += similarity.apply(offerModel.toLowerCase(), item.getModel().toLowerCase());
//                            }

//                            if (offerCategory.equalsIgnoreCase("processor")) {
//                                if (item.getProcessor() != null){
//                                    score += similarity.apply(offerModel.toLowerCase(), item.getProcessor().getBase_clock().toLowerCase());
//                                    score += similarity.apply(offerModel.toLowerCase(), item.getProcessor().getSocket_type().toLowerCase());
////                                    score += similarity.apply(offerModel.toLowerCase(), item.getProcessor().getCores());
//                                }
//                            } else if (offerCategory.equalsIgnoreCase("graphics_card")) {
//                                if (item.getGraphicsCard() != null){
//                                    score += similarity.apply(offerModel.toLowerCase(), item.getGraphicsCard().getVram().toString());
//                                }
//
//                            }

//                            if (score > bestScore) {
//                                bestScore = score;
//                                bestItem = item;
//                                log.info("Przypisano ofertę {},  do podzespołu: {}, dopasowanie punktowe: {}", offerModel, bestItem.getModel(), bestScore);
//                            } else {
//                                log.debug("Oferta nie została przypisana z powodu zbyt niskiego wyniku dopasowania: {}. Oferta: model={}, brand={}",
//                                        bestScore, offerModel, offerBrand);
//                            }
//                        }
//                        // Próg dopasowania, np. 0.7
//                        if (bestItem != null) {
//                            offer.setItem(bestItem);
//                            bestItem.getOffers().add(offer);
//
//                            itemRepository.save(bestItem);
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
