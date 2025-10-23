package org.example.backend_pcbuild.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.example.backend_pcbuild.Components.dto.*;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

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
    private final ShopRepository shopRepository;

    private final RestClient restClient = RestClient.create();

    /**
     * Retrieves a map containing components, where the key is a string identifier of the component
     * and the value is a list of objects representing the component details.
     *
     * The data is fetched from an external HTTP service located at the URL
     * http://127.0.0.1:5000/installComponents.
     *
     * @return a map where the keys are string identifiers of components and
     *         the values are lists of objects representing the component details
     */
    public Map<String, List<Object>> fetchComponentsAsMap() {
        return restClient.get()
                .uri("http://127.0.0.1:5000/installComponents")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void fetchOffersAsMap(List<String> shops) {
        restClient.post()
                .uri("http://127.0.0.1:5000/offers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("shops", shops))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Persists various hardware components into their respective repositories based on their type.
     * Each component is matched with an existing `Item` in the database by its brand and model.
     * If no matching `Item` exists, a new one is created. Then, the specific component (e.g., processor,
     * storage, motherboard, etc.) is associated with the `Item` and saved.
     *
     * @param components a map containing component types as keys (e.g., "processor", "storage") and
     *                   lists of objects as values. Each object is expected to be a map of attributes
     *                   describing a specific component.
     */
    @Transactional
    public void saveBasedComponents(Map<String, List<Object>> components) {
        if (components == null || components.isEmpty()) return;

        for (Map.Entry<String, List<Object>> entry : components.entrySet()) {
            final String type = entry.getKey() == null ? "" : entry.getKey().toLowerCase(Locale.ROOT);
            final List<Object> payload = entry.getValue();
            if (payload == null) continue;

            for (Object object : payload) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) object;
                    if (data == null) continue;

                    String brand = getStringValue(data, "brand");
                    String model = getStringValue(data, "model");
                    if (brand == null || model == null) {
                        continue;
                    }
//                    log.debug("Processing component type={} brand={} model={}", type, brand, model);

                    Item item = itemRepository.findByBrandAndModel(brand, model)
                            .orElseGet(() -> {
                                Item it = new Item();
                                it.setBrand(brand);
                                it.setModel(model);
//                                Item saved = itemRepository.save(it);
//                                log.debug("Created new Item id={} brand={} model={}", saved.getId(), brand, model);
//                                return saved;
                                return it;
                            });


                    switch (type) {
                        case "processor": {
                            if (item.getProcessor() != null) break;
                            Processor p = new Processor();
                            p.setCores(getIntegerValue(data, "cores"));
                            p.setThreads(getIntegerValue(data, "threads"));
                            p.setSocket_type(getStringValue(data, "socket"));
                            p.setBase_clock(getStringValue(data, "base_clock"));
                            p.setItem(item);
                            item.setItemType(ItemType.PROCESSOR);
                            item.setProcessor(p);
//                            processorRepository.save(p);
                            itemRepository.save(item);
                            break;
                        }
                        case "storage": {
                            if (item.getStorage() != null) break;
                            Storage s = new Storage();
                            s.setCapacity(getDoubleValue(data, "capacity"));
                            s.setItem(item);
                            item.setStorage(s);
                            item.setItemType(ItemType.STORAGE);
//                            storageRepository.save(s);
                            itemRepository.save(item);

                            break;
                        }
                        case "motherboard": {
                            if (item.getMotherboard() != null) break;
                            Motherboard mb = new Motherboard();
                            mb.setChipset(getStringValue(data, "chipset"));
                            mb.setFormat(getStringValue(data, "format"));
                            mb.setMemoryType(getStringValue(data, "memory_type"));
                            mb.setSocketType(getStringValue(data, "socket_motherboard"));
                            mb.setRamSlots(getIntegerValue(data, "ramslots"));
                            mb.setRamCapacity(getIntegerValue(data, "memory_capacity"));
                            mb.setItem(item);
                            item.setMotherboard(mb);
                            item.setItemType(ItemType.MOTHERBOARD);
//                            motherboardRepository.save(mb);
                            itemRepository.save(item);

                            break;
                        }
                        case "power_supply": {
                            if (item.getPowerSupply() != null) break;
                            PowerSupply ps = new PowerSupply();
                            Integer maxW = getIntegerValue(data, "maxPowerWatt");
                            if (maxW == null) maxW = getIntegerValue(data, "max_power_watt");
                            ps.setMaxPowerWatt(maxW);
                            ps.setItem(item);
                            item.setPowerSupply(ps);
                            item.setItemType(ItemType.POWER_SUPPLY);
//                            powerSupplyRepository.save(ps);
                            itemRepository.save(item);

                            break;
                        }
                        case "cpu_cooler": {
                            if (item.getCooler() != null) break;
                            Cooler cooler = new Cooler();
                            Object socketsObj = data.get("sockets");
                            if (socketsObj instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<String> sockets = (List<String>) socketsObj;
                                cooler.setSocketTypes(sockets);
                            } else if (socketsObj instanceof String s) {
                                List<String> sockets = Arrays.stream(s.split(","))
                                        .map(String::trim)
                                        .filter(v -> !v.isEmpty())
                                        .toList();
                                cooler.setSocketTypes(sockets);
                            }
                            cooler.setItem(item);
                            item.setCooler(cooler);
                            item.setItemType(ItemType.CPU_COOLER);
//                            coolerRepository.save(cooler);
                            itemRepository.save(item);

                            break;
                        }
                        case "graphics_card": {
                            if (item.getGraphicsCard() != null) break;
                            GraphicsCard g = new GraphicsCard();
                            g.setGddr(getStringValue(data, "gddr"));
                            g.setPower_draw(getDoubleValue(data, "power_draw"));
                            g.setVram(getIntegerValue(data, "vram"));
                            g.setItem(item);
                            item.setGraphicsCard(g);
                            item.setItemType(ItemType.GRAPHICS_CARD);
//                            graphicsCardRepository.save(g);
                            itemRepository.save(item);

                            break;
                        }
                        case "case": {
                            if (item.getCase_() != null) break;
                            Case c = new Case();
                            c.setFormat(getStringValue(data, "format"));
                            c.setItem(item);
                            item.setCase_(c);
                            item.setItemType(ItemType.CASE_PC);
                            itemRepository.save(item);
//                            caseRepository.save(c);
                            break;
                        }
                        case "ram": {
                            if (item.getMemory() != null) break;
                            Memory m = new Memory();
                            m.setCapacity(getIntegerValue(data, "capacity"));
                            m.setType(getStringValue(data, "type"));
                            m.setSpeed(getStringValue(data, "speed"));
                            m.setLatency(getStringValue(data, "latency"));
                            m.setItem(item);
                            item.setMemory(m);
                            item.setItemType(ItemType.MEMORY);
//                            memoryRepository.save(m);
                            itemRepository.save(item);
                            break;
                        }
                        default:
                            break;
                    }
                } catch (ClassCastException cce) {
                     log.warn("Invalid payload entry for type {}: {}", entry.getKey(), cce.getMessage());
                } catch (Exception e) {
                     log.error("Failed to save component of type {}: {}", entry.getKey(), e.getMessage(), e);
                    throw e;
                }
            }
        }
    }


    /**
     * Retrieves all components from various repositories, converts them to their corresponding DTOs,
     * and groups them into a map categorized by component type.
     *
     * @return a map where the keys represent the component types (e.g., "graphicsCards", "processors", etc.)
     * and the values are lists of the corresponding component DTOs.
     */
    public Map<String, List<?>> getAllOffers() {
        Map<String, List<?>> result = new LinkedHashMap<>();

        List<GraphicsCardDto> gpus = graphicsCardRepository.findAll().stream()
                .flatMap(gc -> gc.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> ComponentMapper.toDto(gc, offer)))
                . toList();
//        System.out.println(gpus);
        List<ProcessorDto> processors = processorRepository.findAll().stream()
                .flatMap(cpu -> cpu.getItem().getOffers().stream()
                .filter(Offer::getIsVisible)
                        .map(offer -> ComponentMapper.toDto(cpu, offer)))
                .toList();
//        System.out.println(processors);
        List<CoolerDto> coolers = coolerRepository.findAll().stream()
                .flatMap(c -> c.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> ComponentMapper.toDto(c, offer)))
                .toList();
//        System.out.println(coolers);
        List<MemoryDto> memories = memoryRepository.findAll().stream()
                .flatMap(m -> m.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> ComponentMapper.toDto(m, offer)))
                .toList();
//        System.out.println(memories);
        List<MotherboardDto> motherboards = motherboardRepository.findAll().stream()
                .flatMap(mb -> mb.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> ComponentMapper.toDto(mb, offer)))
                .toList();
//        System.out.println(motherboards);
        List<PowerSupplyDto> powerSupplies = powerSupplyRepository.findAll().stream()
                .flatMap(ps -> ps.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> ComponentMapper.toDto(ps, offer)))
                .toList();
//        System.out.println(powerSupplies);
        List<StorageDto> storages = storageRepository.findAll().stream()
                .flatMap(s -> s.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> ComponentMapper.toDto(s, offer)))
                .toList();
//        System.out.println(storages);
        List<CaseDto> casesPc = caseRepository.findAll().stream()
                .flatMap(c -> c.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> ComponentMapper.toDto(c, offer)))
                .toList();
//        System.out.println(casesPc);
        result.put("graphicsCards", gpus);
        result.put("processors", processors);
        result.put("coolers", coolers);
        result.put("memories", memories);
        result.put("motherboards", motherboards);
        result.put("powerSupplies", powerSupplies);
        result.put("storages", storages);
        result.put("cases", casesPc);

        return result;
    }

    private static final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

    /**
     * Saves all offers provided in the input map by matching them with the appropriate categories
     * and items, and then persisting the matching offers and items into the repository.
     * This method processes and categorizes offers, checks for duplicates, and assigns them to
     * the best matching items when applicable.
     *
     * @param offers a map where the key is the offer category name and the value is the list of
     *                   offer data objects to be processed and saved
     */
    @Transactional
    public Map<String, Integer> saveAllOffers(Map<String, List<Object>> offers, ShopOfferUpdate update) {

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

        int totalSaved = 0;
        int totalDeleted = 0;
        int totalSkipped = 0;

        Map<String, Integer> offersSaved = new HashMap<>();
        for (String category : offers.keySet()) {
            offersSaved.put(category, 0);
        }

        for (Map.Entry<String, List<Object>> entry : offers.entrySet()) {
            String offerCategory = entry.getKey().toLowerCase();
            List<?> itemsForCategory = categoryMap.getOrDefault(offerCategory, Collections.emptyList());

//            System.out.println("Processing category: " + offerCategory + " with " + entry.getValue().size() + " offers");

            for (Object object : entry.getValue()) {
                try {
                    Map<String, Object> componentData = (Map<String, Object>) object;
                    Offer offer = buildOfferFromData(componentData);

                    String url = offer.getWebsiteUrl();

                    Optional<Offer> existingOfferOpt = offerRepository.findByWebsiteUrl(url);
                    if (existingOfferOpt.isPresent()) {
                        totalSkipped++;
                        continue;
                    }

                    if (offer.getCondition() == null || offer.getShop() == null || offer.getPhotoUrl() == null || url == null) {
//                        System.out.println("Skipping offer - missing required fields: " + componentData);
                        totalSkipped++;
                        continue;
                    }

                    Item bestItem = offerMatchingService.matchOfferToItem(offerCategory, componentData, itemsForCategory);
                    if (bestItem != null) {
                        offer.setItem(bestItem);
                        bestItem.getOffers().add(offer);
                          Item item =  itemRepository.save(bestItem);

                          OfferShopOfferUpdate offerUpdate = new OfferShopOfferUpdate();
                          offerUpdate.setOffer(offer);
                          offerUpdate.setShopOfferUpdate(update);

                          offer.getOfferShopOfferUpdates().add(offerUpdate);
                          update.getOfferShopOfferUpdates().add(offerUpdate);

                          offerRepository.save(offer);

                        offersSaved.put(offerCategory, offersSaved.get(offerCategory) + 1);
                        totalSaved++;
//                        System.out.println("Saved offer for: " + bestItem.getBrand() + " " + bestItem.getModel());
                    } else {
//                        System.out.println("No matching item found for offer: " + componentData.get("brand") + " " + componentData.get("model"));
                        totalSkipped++;
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return offersSaved;
    }


    /**
     * Builds and returns an Offer object using the provided component data.
     *
     * @param componentData a map containing key-value pairs of component data used to construct the Offer object.
     *                      Expected keys include "status", "price", "shop", "img", and "url".
     * @return an Offer object populated with data from the componentData map. If specific data is missing or invalid,
     *         corresponding properties may be null or set to default values.
     */
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

        String shopName = (String) componentData.get("shop");
        String img = (String) componentData.get("img");
        String url = (String) componentData.get("url");

        if (shopName != null && !shopName.isBlank()) {
            Shop shop = shopRepository.findByNameIgnoreCase(shopName)
                    .orElseGet(() -> {
                        Shop s = new Shop();
                        s.setName(shopName.trim());
                        return shopRepository.save(s);
                    });
            offer.setShop(shop);
        }


        offer.setPhotoUrl(img);
        offer.setWebsiteUrl(url);
        //
        offer.setIsVisible(true);

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
