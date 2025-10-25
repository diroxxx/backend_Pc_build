package org.example.backend_pcbuild.Offer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.example.backend_pcbuild.Offer.dto.*;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OfferService {

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


    @Transactional
    public void deleteOffersByUrl(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        offerRepository.deleteByWebsiteUrlIn(urls);
    }

    public void softDeleteByUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        List<Offer> offersToDelete = offerRepository.findAllByWebsiteUrlIn(urls);

        for ( Offer offer : offersToDelete) {
            for ( String url : urls ) {
                if (offer.getWebsiteUrl().equals(url)){
                    offer.setIsVisible(false);
                }
            }
        }
        offerRepository.saveAll(offersToDelete);

    }

    public Map<String, List<?>> getAllOffers() {
        Map<String, List<?>> result = new LinkedHashMap<>();

        List<GraphicsCardDto> gpus = graphicsCardRepository.findAll().stream()
                .flatMap(gc -> gc.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(gc, offer)))
                . toList();
//        System.out.println(gpus);
        List<ProcessorDto> processors = processorRepository.findAll().stream()
                .flatMap(cpu -> cpu.getItem().getOffers().stream()
                .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(cpu, offer)))
                .toList();
//        System.out.println(processors);
        List<CoolerDto> coolers = coolerRepository.findAll().stream()
                .flatMap(c -> c.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(c, offer)))
                .toList();
//        System.out.println(coolers);
        List<MemoryDto> memories = memoryRepository.findAll().stream()
                .flatMap(m -> m.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(m, offer)))
                .toList();
//        System.out.println(memories);
        List<MotherboardDto> motherboards = motherboardRepository.findAll().stream()
                .flatMap(mb -> mb.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(mb, offer)))
                .toList();
//        System.out.println(motherboards);
        List<PowerSupplyDto> powerSupplies = powerSupplyRepository.findAll().stream()
                .flatMap(ps -> ps.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(ps, offer)))
                .toList();
//        System.out.println(powerSupplies);
        List<StorageDto> storages = storageRepository.findAll().stream()
                .flatMap(s -> s.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(s, offer)))
                .toList();
//        System.out.println(storages);
        List<CaseDto> casesPc = caseRepository.findAll().stream()
                .flatMap(c -> c.getItem().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(c, offer)))
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


    @Transactional
    public void saveAllOffers(Map<String, List<Object>> offers, ShopOfferUpdate update) {

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

                          OfferShopOfferUpdate offerUpdate = new OfferShopOfferUpdate();
                          offerUpdate.setOffer(offer);
                          offerUpdate.setShopOfferUpdate(update);

                          offer.getOfferShopOfferUpdates().add(offerUpdate);
                          update.getOfferShopOfferUpdates().add(offerUpdate);


                          itemRepository.save(bestItem);
//                          offerRepository.save(offer);

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
    }


    private Offer buildOfferFromData(Map<String, Object> componentData) {
        Offer offer = new Offer();

        String statusString = (String) componentData.get("status");
        if (statusString != null) {
            try {
                offer.setCondition(ItemCondition.valueOf(statusString));
            } catch (IllegalArgumentException ignored) {}
        }

        offer.setPrice(parsePrice(componentData.get("price")));
        offer.setPhotoUrl((String) componentData.get("img"));
        offer.setWebsiteUrl((String) componentData.get("url"));
        offer.setIsVisible(true);

        String shopName = (String) componentData.get("shop");
        if (shopName != null && !shopName.isBlank()) {
            Shop shop = shopRepository.findByNameIgnoreCase(shopName)
                    .orElseThrow(() -> new IllegalStateException("Unknown shop: " + shopName));
            offer.setShop(shop);
        } else {
            throw new IllegalArgumentException("Missing shop name in component data");
        }

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





}
