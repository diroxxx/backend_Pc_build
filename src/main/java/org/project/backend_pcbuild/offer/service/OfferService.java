package org.project.backend_pcbuild.offer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.project.backend_pcbuild.offer.dto.*;
import org.project.backend_pcbuild.offer.model.Brand;
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
    private final ComponentRepository componentRepository;
    private final MemoryRepository memoryRepository;
    private final MotherboardRepository motherboardRepository;
    private final ProcessorRepository processorRepository;
    private final StorageRepository storageRepository;
    private final OfferRepository offerRepository;
    private final OfferMatchingService offerMatchingService;
    private final ShopRepository shopRepository;
    private final BrandRepository brandRepository;

    private static final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();


    @Transactional
    public void deleteOffersByUrl(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        offerRepository.deleteByWebsiteUrlIn(urls);
    }
   public Optional<Offer> findBestForCpu(OfferRepository repo, Component comp, Double budget) {
    if (comp == null) return Optional.empty();
    
    Optional<Offer> exactMatch = findExactCpuOffer(repo, comp, budget);
    if (exactMatch.isPresent()) {
        return exactMatch;
    }
    
    if (comp.getProcessor() != null && comp.getProcessor().getBenchmark() != null) {
        return findSimilarCpuOffer(repo, comp.getProcessor().getBenchmark(), budget);
    }
    
    return Optional.empty();
}

private Optional<Offer> findExactCpuOffer(OfferRepository repo, Component comp, Double budget) {
    if (budget == null) {
        List<Offer> list = repo.findByComponentOrderByPriceAsc(comp);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    } else {
        List<Offer> list = repo.findByComponentOrderByBudgetPriceAsc(comp.getId(), budget);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}

private Optional<Offer> findSimilarCpuOffer(OfferRepository repo, double benchmark, Double budget) {
    double minBenchmark = benchmark * 0.95; 
    double maxBenchmark = benchmark * 1.05; 
    
    if (budget == null) {
        List<Offer> list = repo.findByCpuBenchmarkRangeOrderByPriceAsc(minBenchmark, maxBenchmark);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    } else {
        List<Offer> list = repo.findByCpuBenchmarkRangeAndBudgetOrderByPriceAsc(minBenchmark, maxBenchmark, budget);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}

public Optional<Offer> findBestForGpuModel(OfferRepository repo, GpuModel gm, Double budget) {
    if (gm == null) return Optional.empty();
    
    Optional<Offer> exactMatch = findExactGpuOffer(repo, gm, budget);
    if (exactMatch.isPresent()) {
        return exactMatch;
    }
    
    Double avgBenchmark = getAverageBenchmarkForGpuModel(gm);
    if (avgBenchmark != null) {
        return findSimilarGpuOffer(repo, avgBenchmark, budget);
    }
    
    return Optional.empty();
}

private Optional<Offer> findExactGpuOffer(OfferRepository repo, GpuModel gm, Double budget) {
    if (budget == null) {
        List<Offer> list = repo.findByGpuModelOrderByPriceAsc(gm);
        return list.stream().findFirst();
    } else {
        List<Offer> list = repo.findByGpuModelAndPriceLessThanEqualOrderByPriceAsc(gm, budget, PageRequest.of(0, 1));
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

private Optional<Offer> findSimilarGpuOffer(OfferRepository repo, double benchmark, Double budget) {
    double minBenchmark = benchmark * 0.95; 
    double maxBenchmark = benchmark * 1.05;
    
    if (budget == null) {
        List<Offer> list = repo.findByGpuBenchmarkRangeOrderByPriceAsc(minBenchmark, maxBenchmark);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    } else {
        List<Offer> list = repo.findByGpuBenchmarkRangeAndBudgetOrderByPriceAsc(minBenchmark, maxBenchmark, budget);
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


    @Value("${app.search.useFullTextSearch:false}")
    private boolean useFullTextSearch;
    public Page<BaseOfferDto> getAllOffersV3(Pageable pageable,
                                             ComponentType componentType,
                                             String brand,
                                             Double minPrize,
                                             Double maxPrize,
                                             ComponentCondition componentCondition,
                                             String shopName,
                                             String querySearch) {

        Page<Offer> page;

//        String componentTypeStr = (componentType != null) ? componentType.name() : null;
//        String componentConditionStr = (componentCondition != null) ? componentCondition.name() : null;
//        String qs = (querySearch != null && !querySearch.trim().isEmpty()) ? querySearch.trim() : null;

        if (useFullTextSearch ) {
            page =  offerRepository.findOfferByFiltersProd(
                    componentType,
                    brand, minPrize,
                    maxPrize,
                    componentCondition,
                    shopName,
                    querySearch,
                    pageable
            );
        } else {
            page =  offerRepository.findOfferByFiltersDev(
                    componentType,
                    brand, minPrize,
                    maxPrize,
                    componentCondition,
                    shopName,
                    querySearch,
                    pageable
            );
        }

        List<BaseOfferDto> dtos = page.getContent().stream()
                .map(offer -> {
                    Component c = offer.getComponent();
                    if (c == null) return null;
                    switch (c.getComponentType()) {
                        case PROCESSOR:
                            return OfferComponentMapper.toDto(c.getProcessor(), offer);
                        case GRAPHICS_CARD:
                            return OfferComponentMapper.toDto(c.getGraphicsCard(), offer);
                        case MEMORY:
                            return OfferComponentMapper.toDto(c.getMemory(), offer);

                        case STORAGE:
                            return OfferComponentMapper.toDto(c.getStorage(), offer);
                        case CASE_PC:
                            return OfferComponentMapper.toDto(c.getCase_(), offer);
                        case CPU_COOLER:
                            return OfferComponentMapper.toDto(c.getCooler(), offer);

                            case MOTHERBOARD:
                            return OfferComponentMapper.toDto(c.getMotherboard(), offer);

                            case POWER_SUPPLY:
                            return OfferComponentMapper.toDto(c.getPowerSupply(), offer);

                            default:
                                return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();


//        Stream<BaseOfferDto> stream = result.stream();


//        if (querySearch != null && !querySearch.isBlank()) {
//            String query = querySearch.toLowerCase().replaceAll("[^a-z0-9 ]", " ");
//            stream = stream.filter(o -> {
//                String title = o.getTitle().toLowerCase().replaceAll("[^a-z0-9 ]", " ");
//                return Arrays.stream(title.split("\\s+"))
//                        .anyMatch(word -> similarity.apply(word, query) > 0.85)
//                        || similarity.apply(title, query) > 0.85;
//            });
//        }


//        if (sortBy == SortByOffers.CHEAPEST)
//            stream = stream.sorted(Comparator.comparing(BaseOfferDto::getPrice));
//        else if (sortBy == SortByOffers.EXPENSIVE)
//            stream = stream.sorted(Comparator.comparing(BaseOfferDto::getPrice).reversed());
//        else if (sortBy == SortByOffers.NEWEST)
//            stream = stream.sorted(Comparator.comparing(BaseOfferDto::getId).reversed());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }


    @Transactional
    public void saveOffersTemplate(List<ComponentOfferDto> offers, ShopOfferUpdate update) {

        Set<String> brands = new HashSet<>(brandRepository.findAll()).stream().map(Brand::getName).collect(Collectors.toSet());

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

            //if a brand from a database doesn't exist in title, create new component and offer with status
//            for (String brand : brands) {
//                if ( !offerDto.getTitle().toLowerCase().contains(brand.toLowerCase())) {
//
//                }
//            }

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

        boolean isNewComponent = false;

        if (bestComponent.isEmpty()) {
//            Component newComponent = new Component();
//
//            newComponent.setModel("to change");
//            newComponent.setComponentType(category);

//            Optional<Brand> brandIgnoreCase = brandRepository.findByNameIgnoreCase(offerDto.getBrand());
//            if (brandIgnoreCase.isPresent()) {
//                newComponent.setBrand(brandIgnoreCase.get());
//            }
//            bestComponent = newComponent;
//            componentRepository.save(newComponent);
//            isNewComponent = true;

            System.out.println("No matching component found for: " +
                    offerDto.getBrand() + " " + offerDto.getModel() + " - SKIPPING");
            return false;
        }

        Offer offer = buildOfferConnectToShop(offerDto);

//        System.out.println("Matched component: " +
//                bestComponent.getBrand().getName() + " " + bestComponent.getModel());


        offer.setComponent(bestComponent.get());
        bestComponent.get().getOffers().add(offer);

        OfferShopOfferUpdate offerUpdate = new OfferShopOfferUpdate();
        offerUpdate.setOffer(offer);
        offerUpdate.setShopOfferUpdate(update);

//        if (isNewComponent) {
//            offerUpdate.setUpdateChangeType(UpdateChangeType.RECHECK);
//        } else {
//
//        }
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
            shop.getOffers().add(offer);
        } else {
            throw new IllegalArgumentException("Missing shop name in component data");
        }

        return offer;
    }
}
