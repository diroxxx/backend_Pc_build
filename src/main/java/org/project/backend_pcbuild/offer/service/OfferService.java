package org.project.backend_pcbuild.offer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.backend_pcbuild.offer.dto.*;
import org.project.backend_pcbuild.offer.model.Offer;
import org.project.backend_pcbuild.offer.model.Shop;
import org.project.backend_pcbuild.offer.repository.BrandRepository;
import org.project.backend_pcbuild.offer.repository.OfferRepository;
import org.project.backend_pcbuild.offer.repository.ShopRepository;
import org.project.backend_pcbuild.offersUpdates.model.OfferShopOfferUpdate;
import org.project.backend_pcbuild.offersUpdates.model.ShopOfferUpdate;
import org.project.backend_pcbuild.offersUpdates.model.UpdateChangeType;
import org.project.backend_pcbuild.pcComponents.model.*;
import org.project.backend_pcbuild.pcComponents.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OfferService {

    private final PowerSupplyRepository powerSupplyRepository;
    private final CaseRepository caseRepository;
    private final CoolerRepository coolerRepository;
    private final GraphicsCardRepository graphicsCardRepository;
    private final MemoryRepository memoryRepository;
    private final MotherboardRepository motherboardRepository;
    private final ProcessorRepository processorRepository;
    private final StorageRepository storageRepository;
    private final UnknownComponentRepository unknownComponentRepository;
    private final OfferRepository offerRepository;
    private final OfferMatchingService offerMatchingService;
    private final ShopRepository shopRepository;
    private final BrandRepository brandRepository;



    public Optional<Offer> findBestForCpu(Component comp, Double budget) {
        if (comp == null) return Optional.empty();

        Optional<Offer> exactMatch = findExactCpuOffer(comp, budget);
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        if (comp instanceof Processor processor && processor.getBenchmark() != null) {
            return findSimilarCpuOffer(processor.getBenchmark(), budget);
        }

        return Optional.empty();
    }

private Optional<Offer> findExactCpuOffer(Component comp, Double budget) {
    if (budget == null) {
        List<Offer> list = offerRepository.findByComponentOrderByPriceAsc(comp);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    } else {
        List<Offer> list = offerRepository.findByComponentOrderByBudgetPriceAsc(comp.getId(), budget);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}

private Optional<Offer> findSimilarCpuOffer(double benchmark, Double budget) {
    double minBenchmark = benchmark * 0.95; 
    double maxBenchmark = benchmark * 1.05; 
    
    if (budget == null) {
        List<Offer> list = offerRepository.findByCpuBenchmarkRangeOrderByPriceAsc(minBenchmark, maxBenchmark);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    } else {
        List<Offer> list = offerRepository.findByCpuBenchmarkRangeAndBudgetOrderByPriceAsc(minBenchmark, maxBenchmark, budget);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}

public Optional<Offer> findBestForGpuModel(GpuModel gm, Double budget) {
    if (gm == null) return Optional.empty();
    
    Optional<Offer> exactMatch = findExactGpuOffer(gm, budget);
    if (exactMatch.isPresent()) {
        return exactMatch;
    }
    
    Double avgBenchmark = getAverageBenchmarkForGpuModel(gm);
    if (avgBenchmark != null) {
        return findSimilarGpuOffer(avgBenchmark, budget);
    }
    
    return Optional.empty();
}

private Optional<Offer> findExactGpuOffer(GpuModel gm, Double budget) {
    if (budget == null) {
        List<Offer> list = offerRepository.findByGpuModelOrderByPriceAsc(gm);
        return list.stream().findFirst();
    } else {
        List<Offer> list = offerRepository.findByGpuModelAndPriceLessThanEqualOrderByPriceAsc(gm, budget, PageRequest.of(0, 1));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}

private Double getAverageBenchmarkForGpuModel(GpuModel gm) {
    return gm.getGraphicsCards().stream()
            .map(GraphicsCard::getBenchmark)
            .filter(b -> b != null)
            .findFirst()
            .orElse(null);
}

private Optional<Offer> findSimilarGpuOffer(double benchmark, Double budget) {
    double minBenchmark = benchmark * 0.95; 
    double maxBenchmark = benchmark * 1.05;
    
    if (budget == null) {
        List<Offer> list = offerRepository.findByGpuBenchmarkRangeOrderByPriceAsc(minBenchmark, maxBenchmark);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    } else {
        List<Offer> list = offerRepository.findByGpuBenchmarkRangeAndBudgetOrderByPriceAsc(minBenchmark, maxBenchmark, budget);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
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


    @Value("${app.search.useFullTextSearch:false}")
    private boolean useFullTextSearch;
    public Page<BaseOfferDto> getAllOffersV3(Pageable pageable,
                                             ComponentType componentType,
                                             String brand,
                                             Double minPrize,
                                             Double maxPrize,
                                             ComponentCondition componentCondition,
                                             String shopName,
                                             String querySearch,
                                             Boolean dealOnly) {

        Page<Offer> page;

        page =  offerRepository.findOfferByFiltersProd(
                    componentType,
                    brand, minPrize,
                    maxPrize,
                    componentCondition,
                    shopName,
                    querySearch,
                    dealOnly,
                    pageable
        );


        Set<ComponentType> typesOnPage = page.getContent().stream()
                .map(Offer::getComponent)
                .filter(Objects::nonNull)
                .map(Component::getComponentType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<ComponentType, Double> minPriceByType = new HashMap<>();
        for (ComponentType type : typesOnPage) {
            Double min = offerRepository.findMinPriceByComponentType(type);
            minPriceByType.put(type, min);
        }

        List<BaseOfferDto> dtos = page.getContent().stream()
                .map(offer -> {
                    Component c = offer.getComponent();
                    if (c == null) return null;

                    BaseOfferDto dto = switch (c) {
                        case Processor p    -> OfferComponentMapper.toDto(p, offer);
                        case GraphicsCard g -> OfferComponentMapper.toDto(g, offer);
                        case Memory m       -> OfferComponentMapper.toDto(m, offer);
                        case Storage s      -> OfferComponentMapper.toDto(s, offer);
                        case Case cs        -> OfferComponentMapper.toDto(cs, offer);
                        case Cooler co      -> OfferComponentMapper.toDto(co, offer);
                        case Motherboard mb -> OfferComponentMapper.toDto(mb, offer);
                        case PowerSupply ps -> OfferComponentMapper.toDto(ps, offer);
                        default             -> null;
                    };

                    if (dto != null) {
                        Double minPrice = minPriceByType.get(c.getComponentType());
                        boolean isDeal = false;
                        if (minPrice != null && offer.getPrice() != null) {
                            isDeal = offer.getPrice() <= 1.05 * minPrice;
                        }
                        dto.setIsDeal(isDeal);
                    }

                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();




        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Transactional
    public void saveOffersTemplate(List<ComponentOfferDto> offers, ShopOfferUpdate update) {

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


        offers.forEach(offerDto -> {

            Optional<Offer> existingOfferOpt = offerRepository
                    .findByShopNameAndWebsiteUrlIgnoreCaseTrim(offerDto.getShop(), offerDto.getUrl());


            if (existingOfferOpt.isPresent()) {
                Offer existingOffer = existingOfferOpt.get();

                boolean alreadyLinked = existingOffer.getOfferShopOfferUpdates().stream()
                        .anyMatch(link -> link.getShopOfferUpdate().getId().equals(update.getId()));

                if (alreadyLinked) {
                    System.out.println("Skipping - offer already linked to this update: " + offerDto.getUrl());
                    return;
                }
                System.out.println("Skipping existing offer (no new record for this update): " + offerDto.getUrl());
                return;
            }
            ComponentType category = offerDto.getCategory();
            List<?> itemsForCategory = categoryMap.getOrDefault(category, Collections.emptyList());


            saveOffer(offerDto, update, itemsForCategory, category);

        });
    }

    @Transactional
    public boolean saveOffer(ComponentOfferDto offerDto, ShopOfferUpdate update , List<?> itemsForCategory,ComponentType category) {
        System.out.println("Processing Offer");
        System.out.println("Title: " + offerDto.getTitle());
        System.out.println("Brand: " + offerDto.getBrand());
        System.out.println("Model: " + offerDto.getModel());
        System.out.println("Category: " + offerDto.getCategory());


        Optional<Component> bestComponent = offerMatchingService.matchOfferToComponent(
                category,
                offerDto,
                itemsForCategory
        );

        Offer offer = buildOfferConnectToShop(offerDto);

        if (bestComponent.isEmpty()) {
            UnknownComponent placeholder = unknownComponentRepository.findFirstByModel("UNKNOWN")
                    .orElseGet(() -> {
                        UnknownComponent uc = new UnknownComponent();
                        uc.setModel("UNKNOWN");
                        uc.setBrand(null);
                        uc.setComponentType(category);
                        return unknownComponentRepository.save(uc);
                    });

            offer.setComponent(placeholder);
            placeholder.getOffers().add(offer);

            OfferShopOfferUpdate offerUpdate = new OfferShopOfferUpdate();
            offerUpdate.setOffer(offer);
            offerUpdate.setShopOfferUpdate(update);

            offerUpdate.setUpdateChangeType(UpdateChangeType.ADDED);
            offer.getOfferShopOfferUpdates().add(offerUpdate);
            update.getOfferShopOfferUpdates().add(offerUpdate);

            offerRepository.save(offer);
            System.out.println("Unknown component: " + offerDto.getUrl());
            return true;

        }

        offer.setComponent(bestComponent.get());
        bestComponent.get().getOffers().add(offer);

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

    public List<ComponentStatsDto>  getCountsOffersByComponents() {
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
                offer.setCondition(ComponentCondition.valueOf(statusString));
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
        } else {
            throw new IllegalArgumentException("Missing shop name in component data");
        }

        return offer;
    }

    public Page<BaseOfferDto> getUnknownComponentOffers(Pageable pageable) {
        Page<Offer> page = offerRepository.findOfferWithUnknownComponent(pageable);

        List<BaseOfferDto> dtos = page.getContent().stream()
                .map(offer -> {
                    Component c = offer.getComponent();
                    if (c == null) return null;

                    BaseOfferDto dto = switch (c) {
                        case UnknownComponent uc -> OfferComponentMapper.toDto(uc, offer);
                        default -> null;
                    };

                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

}
