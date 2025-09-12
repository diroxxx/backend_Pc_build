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

        List<Item> itemList = itemRepository.findAll();
//        log.info("Rozpoczęto przypisywanie ofert do podzespołów, liczba kategorii: {}", components.size());
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

//                        log.info("Przetwarzanie oferty: brand={}, model={}, category={}", offerBrand, offerModel, offerCategory);
                        Item bestItem = null;
                        double bestScore = 0.0;


                        String[] offerWords = offerModel.toLowerCase().split(" ");
                        
                        if (offerCategory.equalsIgnoreCase("processor")) {

                            Pattern socketPattern = Pattern.compile("(am4|lga\\d{4}|s\\d{3,4}|fm2|fm1)", Pattern.CASE_INSENSITIVE);
                            Pattern clockPattern = Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\s?ghz)", Pattern.CASE_INSENSITIVE);
                            Pattern coresPattern = Pattern.compile("(\\d+)\\s*(rdzeni|cores)", Pattern.CASE_INSENSITIVE);
                            Pattern threadsPattern = Pattern.compile("(\\d+)\\s*(wątk|threads)", Pattern.CASE_INSENSITIVE);
                            String offerLower = offerModel.toLowerCase();

                            // Wyciąganie cech z oferty
                            String offerSocket = null;
                            String offerClock = null;
                            Integer offerCores = null;
                            Integer offerThreads = null;

                            var mSocket = socketPattern.matcher(offerLower);
                            if (mSocket.find()) offerSocket = mSocket.group(1);

                            var mClock = clockPattern.matcher(offerLower);
                            if (mClock.find()) offerClock = mClock.group(1);

                            var mCores = coresPattern.matcher(offerLower);
                            if (mCores.find()) offerCores = Integer.valueOf(mCores.group(1));

                            var mThreads = threadsPattern.matcher(offerLower);
                            if (mThreads.find()) offerThreads = Integer.valueOf(mThreads.group(1));

                            for(Processor processor : processorList){
                                String[] itemWords = processor.getItem().getModel().toLowerCase().split(" ");
                               
                                int commonWords = 0;
                                for (String ow : offerWords) {
                                    for (String iw : itemWords) {
                                        if (ow.equals(iw)) commonWords++;
                                    }
                                }
                                double score = commonWords * 0.2;

                                if (processor.getItem().getBrand() != null &&
                                        offerBrand.equalsIgnoreCase(processor.getItem().getBrand())) {
                                    score += 0.3;
                                }

                                // Dopasowanie socket
                                if (offerSocket != null && processor.getSocket_type() != null &&
                                        offerSocket.equalsIgnoreCase(processor.getSocket_type())) {
                                    score += 0.3;
                                }

                                // Dopasowanie base_clock
                                if (offerClock != null && processor.getBase_clock() != null &&
                                        offerClock.equalsIgnoreCase(processor.getBase_clock().toLowerCase())) {
                                    score += 0.2;
                                }

                                // Dopasowanie liczby rdzeni
                                if (offerCores != null && processor.getCores() != null &&
                                        offerCores.equals(processor.getCores())) {
                                    score += 0.2;
                                }

                                // Dopasowanie liczby wątków
                                if (offerThreads != null && processor.getThreads() != null &&
                                        offerThreads.equals(processor.getThreads())) {
                                    score += 0.2;
                                }

                                if (score > bestScore) {
                                    bestScore = score;
                                    bestItem = processor.getItem();
//                                    log.info("Przypisano ofertę {},  do podzespołu: {}, dopasowanie punktowe: {}", offerModel, bestItem.getModel(), bestScore);
                                } else {
//                                    log.debug("Oferta nie została przypisana z powodu zbyt niskiego wyniku dopasowania: {}. Oferta: model={}, brand={}",
//                                            bestScore, offerModel, offerBrand);
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
