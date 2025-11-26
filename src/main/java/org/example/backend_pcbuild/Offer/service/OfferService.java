package org.example.backend_pcbuild.Offer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.example.backend_pcbuild.Admin.dto.ComponentOfferDto;
import org.example.backend_pcbuild.Offer.dto.*;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class OfferService {

    private final PowerSupplyRepository powerSupplyRepository;
    private final CaseRepository caseRepository;
    private final CoolerRepository coolerRepository;
    private final GraphicsCardRepository graphicsCardRepository;
    private final ComponentRepository componentRepository;
    private final MemoryRepository memoryRepository;
    private final MotherboardRepository motherboardRepository;
    private final ProcessorRepository processorRepository;
    private final StorageRepository storageRepository;
    private final OfferRepository offerRepository;
    private final OfferMatchingService offerMatchingService;
    private final ShopRepository shopRepository;
    private static final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();


    @Transactional
    public void deleteOffersByUrl(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        offerRepository.deleteByWebsiteUrlIn(urls);
    }

    public List<String> getAllOfferNames() {
        return offerRepository.findDistinctShopNames();
    }

    public Long countAllVisibleOffers() {
       return offerRepository.countOffersByIsVisibleTrue();
    }


    public void softDeleteByUrls(List<String> urls, ShopOfferUpdate shopOfferUpdate) {
        if (urls == null || urls.isEmpty() || shopOfferUpdate == null) return;

        List<Offer> offersToDelete = offerRepository.findAllByWebsiteUrlIn(urls);

        for (Offer offer : offersToDelete) {
            offer.setIsVisible(false);

            boolean alreadyLinkedDeleted = offer.getOfferShopOfferUpdates().stream()
                    .anyMatch(link ->
                            link.getShopOfferUpdate().getId().equals(shopOfferUpdate.getId()) &&
                                    link.getUpdateChangeType() == UpdateChangeType.DELETED
                    );

            if (!alreadyLinkedDeleted) {
                OfferShopOfferUpdate link = new OfferShopOfferUpdate();
                link.setOffer(offer);
                link.setShopOfferUpdate(shopOfferUpdate);
                link.setUpdateChangeType(UpdateChangeType.DELETED);

                offer.getOfferShopOfferUpdates().add(link);
                shopOfferUpdate.getOfferShopOfferUpdates().add(link);
            }
        }

        offerRepository.saveAll(offersToDelete);
    }

    public Map<String, List<?>> getAllOffers() {
        Map<String, List<?>> result = new LinkedHashMap<>();

        List<GraphicsCardDto> gpus = graphicsCardRepository.findAll().stream()
                .flatMap(gc -> gc.getComponent().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(gc, offer)))
                . toList();
//        System.out.println(gpus);
        List<ProcessorDto> processors = processorRepository.findAll().stream()
                .flatMap(cpu -> cpu.getComponent().getOffers().stream()
                .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(cpu, offer)))
                .toList();
//        System.out.println(processors);
        List<CoolerDto> coolers = coolerRepository.findAll().stream()
                .flatMap(c -> c.getComponent().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(c, offer)))
                .toList();
//        System.out.println(coolers);
        List<MemoryDto> memories = memoryRepository.findAll().stream()
                .flatMap(m -> m.getComponent().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(m, offer)))
                .toList();
//        System.out.println(memories);
        List<MotherboardDto> motherboards = motherboardRepository.findAll().stream()
                .flatMap(mb -> mb.getComponent().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(mb, offer)))
                .toList();
//        System.out.println(motherboards);
        List<PowerSupplyDto> powerSupplies = powerSupplyRepository.findAll().stream()
                .flatMap(ps -> ps.getComponent().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(ps, offer)))
                .toList();
//        System.out.println(powerSupplies);
        List<StorageDto> storages = storageRepository.findAll().stream()
                .flatMap(s -> s.getComponent().getOffers().stream()
                        .filter(Offer::getIsVisible)
                        .map(offer -> OfferComponentMapper.toDto(s, offer)))
                .toList();
//        System.out.println(storages);
        List<CaseDto> casesPc = caseRepository.findAll().stream()
                .flatMap(c -> c.getComponent().getOffers().stream()
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

    public Page<BaseOfferDto> getAllOffersV2(Pageable pageable,
                                              ComponentType componentType,
                                              String brand,
                                              Double minPrize,
                                              Double maxPrize,
                                              ItemCondition itemCondition,
                                              String shopName,
                                              SortByOffers sortBy,
                                              String querySearch) {

        List<BaseOfferDto> result = new ArrayList<>();

        if(componentType == null || componentType == ComponentType.GRAPHICS_CARD) {
            List<GraphicsCardDto> gpus = graphicsCardRepository.findAll().stream()
                    .flatMap(gc -> gc.getComponent().getOffers().stream()
                            .filter(Offer::getIsVisible)
                            .map(offer -> OfferComponentMapper.toDto(gc, offer)))
                    . toList();
            result.addAll(gpus);
        }

        if(componentType == null || componentType == ComponentType.PROCESSOR) {
            List<ProcessorDto> processors = processorRepository.findAll().stream()
                    .flatMap(cpu -> cpu.getComponent().getOffers().stream()
                            .filter(Offer::getIsVisible)
                            .map(offer -> OfferComponentMapper.toDto(cpu, offer)))
                    .toList();
            result.addAll(processors);
        }

        if (componentType == null || componentType == ComponentType.CPU_COOLER) {
            List<CoolerDto> coolers = coolerRepository.findAll().stream()
                    .flatMap(c -> c.getComponent().getOffers().stream()
                            .filter(Offer::getIsVisible)
                            .map(offer -> OfferComponentMapper.toDto(c, offer)))
                    .toList();
            result.addAll(coolers);
        }

        if (componentType == null || componentType == ComponentType.MEMORY) {
            List<MemoryDto> memories = memoryRepository.findAll().stream()
                    .flatMap(m -> m.getComponent().getOffers().stream()
                            .filter(Offer::getIsVisible)
                            .map(offer -> OfferComponentMapper.toDto(m, offer)))
                    .toList();
            result.addAll(memories);
        }

        if (componentType == null || componentType == ComponentType.MOTHERBOARD) {
            List<MotherboardDto> motherboards = motherboardRepository.findAll().stream()
                    .flatMap(mb -> mb.getComponent().getOffers().stream()
                            .filter(Offer::getIsVisible)
                            .map(offer -> OfferComponentMapper.toDto(mb, offer)))
                    .toList();
            result.addAll(motherboards);
        }

        if (componentType == null || componentType == ComponentType.POWER_SUPPLY) {
            List<PowerSupplyDto> powerSupplies = powerSupplyRepository.findAll().stream()
                    .flatMap(ps -> ps.getComponent().getOffers().stream()
                            .filter(Offer::getIsVisible)
                            .map(offer -> OfferComponentMapper.toDto(ps, offer)))
                    .toList();
            result.addAll(powerSupplies);
        }

        if (componentType == null || componentType == ComponentType.STORAGE) {
            List<StorageDto> storages = storageRepository.findAll().stream()
                    .flatMap(s -> s.getComponent().getOffers().stream()
                            .filter(Offer::getIsVisible)
                            .map(offer -> OfferComponentMapper.toDto(s, offer)))
                    .toList();
            result.addAll(storages);
        }

        if (componentType == null || componentType == ComponentType.CASE_PC) {

            List<CaseDto> casesPc = caseRepository.findAll().stream()
                    .flatMap(c -> c.getComponent().getOffers().stream()
                            .filter(Offer::getIsVisible)
                            .map(offer -> OfferComponentMapper.toDto(c, offer)))
                    .toList();
            result.addAll(casesPc);
        }

        Stream<BaseOfferDto> stream = result.stream();
        if (brand != null && !brand.isBlank()) {
            stream = stream.filter(o -> o.getBrand() != null && o.getBrand().equalsIgnoreCase(brand));
        }

        if (shopName != null && !shopName.isBlank()) {
            stream = stream.filter(o -> o.getShopName() != null && o.getShopName().equalsIgnoreCase(shopName));
        }

        if (minPrize != null) {
            stream = stream.filter(o -> o.getPrice() >= minPrize);
        }

        if (maxPrize != null) {
            stream = stream.filter(o -> o.getPrice() <= maxPrize);
        }

        if (itemCondition != null) {
            stream = stream.filter(o -> o.getCondition() == itemCondition);
        }

        if (querySearch != null && !querySearch.isBlank()) {
            String query = querySearch.toLowerCase().replaceAll("[^a-z0-9 ]", " ");
            stream = stream.filter(o -> {
                String title = o.getTitle().toLowerCase().replaceAll("[^a-z0-9 ]", " ");
                return Arrays.stream(title.split("\\s+"))
                        .anyMatch(word -> similarity.apply(word, query) > 0.85)
                        || similarity.apply(title, query) > 0.85;
            });
        }


        if (sortBy == SortByOffers.CHEAPEST)
            stream = stream.sorted(Comparator.comparing(BaseOfferDto::getPrice));
        else if (sortBy == SortByOffers.EXPENSIVE)
            stream = stream.sorted(Comparator.comparing(BaseOfferDto::getPrice).reversed());
        else if (sortBy == SortByOffers.NEWEST)
            stream = stream.sorted(Comparator.comparing(BaseOfferDto::getId).reversed());



        List<BaseOfferDto> filtered = stream.toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<BaseOfferDto> pagedList;
        if (start >= filtered.size()) {
            pagedList = List.of();
        } else {
            pagedList = filtered.subList(start, end);
        }
        return new PageImpl<>(pagedList, pageable, filtered.size());
    }

    @Transactional
    public boolean saveOffer(ComponentOfferDto offerDto, ShopOfferUpdate update) {
        System.out.println("=== Processing Offer ===");
        System.out.println("Title: " + offerDto.getTitle());
        System.out.println("Brand: " + offerDto.getBrand());
        System.out.println("Model: " + offerDto.getModel());
        System.out.println("Category: " + offerDto.getCategory());

        Optional<Offer> existingOfferOpt = offerRepository
                .findByShopNameAndWebsiteUrlIgnoreCaseTrim(offerDto.getShop(), offerDto.getUrl());

        if (existingOfferOpt.isPresent()) {
            Offer existingOffer = existingOfferOpt.get();

            boolean alreadyLinked = existingOffer.getOfferShopOfferUpdates().stream()
                    .anyMatch(link -> link.getShopOfferUpdate().getId().equals(update.getId()));

            if (alreadyLinked) {
                System.out.println("Skipping - offer already linked to this update: " + offerDto.getUrl());
                return false;
            }

            // Dla istniejących ofert NIC nie zapisujemy do OfferShopOfferUpdate.
            // Dzięki temu statystyki z aktualizacji (ADDED) będą liczyć tylko NOWE oferty.
            System.out.println("Skipping existing offer (no new record for this update): " + offerDto.getUrl());
            return false;
        }

        List<GraphicsCard> graphicsCardList = graphicsCardRepository.findAll();
        List<Processor> processorList = processorRepository.findAll();
        List<Memory> memoryList = memoryRepository.findAll();
        List<Motherboard> motherboardList = motherboardRepository.findAll();
        List<PowerSupply> powerSupplyList = powerSupplyRepository.findAll();
        List<Storage> storageList = storageRepository.findAll();
        List<Case> caseList = caseRepository.findAll();
        List<Cooler> coolerList = coolerRepository.findAll();

        Map<ComponentType, List<?>> categoryMap = Map.of(
                ComponentType.GRAPHICS_CARD, graphicsCardList,
                ComponentType.PROCESSOR, processorList,
                ComponentType.MEMORY, memoryList,
                ComponentType.MOTHERBOARD, motherboardList,
                ComponentType.POWER_SUPPLY, powerSupplyList,
                ComponentType.STORAGE, storageList,
                ComponentType.CASE_PC, caseList,
                ComponentType.CPU_COOLER, coolerList
        );

        ComponentType category = offerDto.getCategory();
        List<?> itemsForCategory = categoryMap.getOrDefault(category, Collections.emptyList());

        System.out.println("Items for category " + category + ": " + itemsForCategory.size());

        Component bestComponent = offerMatchingService.matchOfferToComponent(
                category,
                offerDto,
                itemsForCategory
        );

        if (bestComponent == null) {
            System.out.println("No matching component found for: " +
                    offerDto.getBrand() + " " + offerDto.getModel() + " - SKIPPING");
            return false;
        }

        Offer offer = buildOfferConnectToShop(offerDto);

        System.out.println("✓ Matched component: " +
                bestComponent.getBrand().getName() + " " + bestComponent.getModel());
        offer.setComponent(bestComponent);
        bestComponent.getOffers().add(offer);

        // TYLKO dla nowych ofert tworzymy powiązanie z aktualizacją (ADDED)
        OfferShopOfferUpdate offerUpdate = new OfferShopOfferUpdate();
        offerUpdate.setOffer(offer);
        offerUpdate.setShopOfferUpdate(update);
        offerUpdate.setUpdateChangeType(UpdateChangeType.ADDED);

        offer.getOfferShopOfferUpdates().add(offerUpdate);
        update.getOfferShopOfferUpdates().add(offerUpdate);

        offerRepository.save(offer);

        System.out.println("Saved new offer with component: " + offerDto.getUrl());
        return true;
    }
    public  List<ComponentStatsDto>  getCountsOffersByComponents() {
        var totals = offerRepository.getOfferStatsTotal();
        var details = offerRepository.getOfferStatsByComponentAndShop();

        Map<String, Map<String, Long>> byType = new HashMap<>();
        for (var d : details) {
            byType.computeIfAbsent(d.getComponentType(), k -> new HashMap<>())
                    .put(d.getShopName(), d.getCount());
        }

        return totals.stream()
                .map(t -> new ComponentStatsDto(
                        t.getComponentType(),
                        t.getTotal(),
                        byType.getOrDefault(t.getComponentType(), Map.of())
                ))
                .toList();
    }

    private Offer buildOfferConnectToShop(ComponentOfferDto componentData) {
        Offer offer = new Offer();

        String statusString = componentData.getStatus();
        if (statusString != null) {
            try {
                offer.setCondition(ItemCondition.valueOf(statusString));
            } catch (IllegalArgumentException ignored) {}
        }

        offer.setPrice(componentData.getPrice());
        offer.setPhotoUrl(componentData.getImg());
        offer.setWebsiteUrl(componentData.getUrl());
        offer.setIsVisible(true);
        offer.setTitle(componentData.getTitle());
        String shopName = componentData.getShop();

        if (shopName != null && !shopName.isBlank()) {
            Shop shop = shopRepository.findByNameIgnoreCase(shopName)
                    .orElseThrow(() -> new IllegalStateException("Unknown shop: " + shopName));
            offer.setShop(shop);
            shop.getOffers().add(offer);
        } else {
            throw new IllegalArgumentException("Missing shop name in component data");
        }

        return offer;
    }
}
