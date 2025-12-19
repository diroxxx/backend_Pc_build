package org.project.backend_pcbuild.pcComponents.service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.backend_pcbuild.offer.model.Brand;
import org.project.backend_pcbuild.offer.repository.BrandRepository;
import org.project.backend_pcbuild.pcComponents.dto.*;
import org.project.backend_pcbuild.pcComponents.model.*;
import org.project.backend_pcbuild.pcComponents.repository.ComponentRepository;
import org.project.backend_pcbuild.pcComponents.repository.GpuModelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentService {
    private final RestClient restClient = RestClient.create();
    private final ComponentRepository componentRepository;
    private final ItemComponentMapper itemComponentMapper;
    private final BrandRepository brandRepository;
    private final GpuModelRepository gpuModelRepository;

//    public Map<String, List<Object>> fetchComponentsAsMap() {
//        return restClient.get()
//                .uri("http://127.0.0.1:5000/installComponents")
//                .retrieve()
//                .body(new ParameterizedTypeReference<>() {});
//    }


    public Page<BaseItemDto> getComponents(Pageable pageable, ComponentType type, String brand, String searchTerm) {
        Specification<Component> spec = Specification.not(null);

        if (type != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("componentType"), type)
            );
        }
        if (brand != null && !brand.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("brand").get("name")), brand.toLowerCase())
            );
        }
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String term = "%" + searchTerm.toLowerCase() + "%";

            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                List<Predicate> predicates = new ArrayList<>();

                predicates.add(cb.like(cb.lower(root.get("brand").get("name")), term));
                predicates.add(cb.like(cb.lower(root.get("model")), term));

                if (type == null || type == ComponentType.PROCESSOR) {
                    Join<Component, Processor> p = root.join("processor", JoinType.LEFT);
                    predicates.add(cb.like(cb.lower(cb.coalesce(p.get("socketType"), cb.literal(""))), term));
                }
                if (type == null || type == ComponentType.MEMORY) {
                    Join<Component, Memory> m = root.join("memory", JoinType.LEFT);
                    predicates.add(cb.like(cb.lower(cb.coalesce(m.get("type"), cb.literal(""))), term));
                }
                if (type == null || type == ComponentType.GRAPHICS_CARD) {
                    Join<Component, GraphicsCard> g = root.join("graphicsCard", JoinType.LEFT);
                    predicates.add(cb.like(cb.lower(cb.coalesce(g.get("gddr"), cb.literal(""))), term));
                }
                if (type == null || type == ComponentType.MOTHERBOARD) {
                    Join<Component, Motherboard> mb = root.join("motherboard", JoinType.LEFT);
                    predicates.add(cb.like(cb.lower(cb.coalesce(mb.get("chipset"), cb.literal(""))), term));
                }
                if (type == null || type == ComponentType.CASE_PC) {
                    Join<Component, Case> cs = root.join("case_", JoinType.LEFT);
                    predicates.add(cb.like(cb.lower(cb.coalesce(cs.get("format"), cb.literal(""))), term));
                }
                if (type == null || type == ComponentType.POWER_SUPPLY) {
                    Join<Component, PowerSupply> ps = root.join("powerSupply", JoinType.LEFT);
                    predicates.add(cb.like(cb.lower(cb.coalesce(ps.get("type"), cb.literal(""))), term));
                }

                return cb.or(predicates.toArray(new Predicate[0]));
            });
        }

        Page<Component> itemsPage = componentRepository.findAll(spec, pageable);
        return itemsPage.map(this::mapToDto);
    }


    public Page<BaseItemDto> getComponentsV2(Pageable pageable, ComponentType type, String brand, String searchTerm) {

       Page<Component> components = componentRepository.findAllByFilters(brand,type,searchTerm, pageable);

        return components.map(this::mapToDto);
    }





    private BaseItemDto mapToDto(Component component) {
        if (component == null || component.getComponentType() == null) return null;

        switch (component.getComponentType()) {
            case PROCESSOR -> {
                return itemComponentMapper.toDto(component.getProcessor());
            }
            case GRAPHICS_CARD -> {
                return itemComponentMapper.toDto(component.getGraphicsCard());
            }
            case MOTHERBOARD -> {
                return itemComponentMapper.toDto(component.getMotherboard());
            }
            case MEMORY -> {
                return itemComponentMapper.toDto(component.getMemory());
            }
            case CPU_COOLER -> {
                return itemComponentMapper.toDto(component.getCooler());
            }
            case POWER_SUPPLY -> {
                return itemComponentMapper.toDto(component.getPowerSupply());
            }
            case CASE_PC -> {
                return itemComponentMapper.toDto(component.getCase_());
            }
            case STORAGE -> {
                return itemComponentMapper.toDto(component.getStorage());
            }
            default -> {
                return null;
            }
        }
    }

    public List<String> getAllBrands() {
        return componentRepository.findDistinctBrands();


    }
    private Brand getOrCreateBrand(String brandName) {
        if (brandName == null || brandName.isBlank()) {
            throw new IllegalArgumentException("Brand name cannot be null or blank");
        }
        return brandRepository.findByNameIgnoreCase(brandName.trim())
                .orElseGet(() -> {
                    Brand brand = new Brand();
                    brand.setName(brandName.trim());
                    return brandRepository.save(brand);
                });
    }

    private Component getOrCreateComponent(String brandName, String model, ComponentType type) {
        Brand brand = getOrCreateBrand(brandName);
        return componentRepository.findByBrandAndModelIgnoreCase(brand, model)
                .orElseGet(() -> {
                    Component c = new Component();
                    c.setBrand(brand);
                    c.setModel(model);
                    c.setComponentType(type);
                    return c;
                });
    }

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

                    Brand checkBrand =getOrCreateBrand(brand);

                    Component component = componentRepository.findByBrandAndModelIgnoreCase(checkBrand, model).orElseGet(() -> {
                        Component component1 = new Component();
                        component1.setModel(model);
                        component1.setBrand(checkBrand);
                        return component1;
                    });

                    switch (type) {
                        case "processor": {
                            if (component.getProcessor() != null) break;
                            Processor p = new Processor();
                            p.setCores(getIntegerValue(data, "cores"));
                            p.setThreads(getIntegerValue(data, "threads"));
                            p.setSocketType(getStringValue(data, "socket"));
                            p.setBaseClock(getDoubleValue(data, "base_clock"));
                            p.setComponent(component);
                            component.setComponentType(ComponentType.PROCESSOR);
                            component.setProcessor(p);
                            componentRepository.save(component);
                            break;
                        }
                        case "storage": {
                            if (component.getStorage() != null) break;
                            Storage s = new Storage();
                            s.setCapacity(getDoubleValue(data, "capacity"));
                            s.setComponent(component);
                            component.setStorage(s);
                            component.setComponentType(ComponentType.STORAGE);
                            componentRepository.save(component);

                            break;
                        }
                        case "motherboard": {
                            if (component.getMotherboard() != null) break;
                            Motherboard mb = new Motherboard();
                            mb.setChipset(getStringValue(data, "chipset"));
                            mb.setFormat(getStringValue(data, "format"));
                            mb.setMemoryType(getStringValue(data, "memory_type"));
                            mb.setSocketType(getStringValue(data, "socket_motherboard"));
                            mb.setRamSlots(getIntegerValue(data, "ramslots"));
                            mb.setRamCapacity(getIntegerValue(data, "memory_capacity"));
                            mb.setComponent(component);
                            component.setMotherboard(mb);
                            component.setComponentType(ComponentType.MOTHERBOARD);
//                            motherboardRepository.save(mb);
                            componentRepository.save(component);

                            break;
                        }
                        case "power_supply": {
                            if (component.getPowerSupply() != null) break;
                            PowerSupply ps = new PowerSupply();
                            Integer maxW = getIntegerValue(data, "maxPowerWatt");
                            if (maxW == null) maxW = getIntegerValue(data, "max_power_watt");
                            ps.setMaxPowerWatt(maxW);
                            ps.setComponent(component);
                            component.setPowerSupply(ps);
                            component.setComponentType(ComponentType.POWER_SUPPLY);
//                            powerSupplyRepository.save(ps);
                            componentRepository.save(component);

                            break;
                        }
                        case "cpu_cooler": {
                            if (component.getCooler() != null) break;
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
                            cooler.setComponent(component);
                            component.setCooler(cooler);
                            component.setComponentType(ComponentType.CPU_COOLER);
//                            coolerRepository.save(cooler);
                            componentRepository.save(component);

                            break;
                        }
                        case "graphics_card": {
                            if (component.getGraphicsCard() != null) break;
                            GraphicsCard g = new GraphicsCard();
                            g.setGddr(getStringValue(data, "gddr"));
                            g.setPowerDraw(getDoubleValue(data, "power_draw"));
                            g.setVram(getIntegerValue(data, "vram"));
                            g.setComponent(component);
                            component.setGraphicsCard(g);
                            component.setComponentType(ComponentType.GRAPHICS_CARD);
//                            graphicsCardRepository.save(g);
                            componentRepository.save(component);

                            break;
                        }
                        case "case": {
                            if (component.getCase_() != null) break;
                            Case c = new Case();
                            c.setFormat(getStringValue(data, "format"));
                            c.setComponent(component);
                            component.setCase_(c);
                            component.setComponentType(ComponentType.CASE_PC);
                            componentRepository.save(component);
//                            caseRepository.save(c);
                            break;
                        }
                        case "ram": {
                            if (component.getMemory() != null) break;
                            Memory m = new Memory();
                            m.setCapacity(getIntegerValue(data, "capacity"));
                            m.setType(getStringValue(data, "type"));
                            m.setSpeed(getIntegerValue(data, "speed"));
                            m.setLatency(getIntegerValue(data, "latency"));
                            m.setComponent(component);
                            component.setMemory(m);
                            component.setComponentType(ComponentType.MEMORY);
//                            memoryRepository.save(m);
                            componentRepository.save(component);
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

    @Transactional
    public void saveComponents(List<? extends BaseItemDto> components) {
        if (components == null || components.isEmpty()) {
            return;
        }
        components.forEach(this::saveComponent);
    }

    private <T> void setIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    public void saveComponent(BaseItemDto dto) {


            if (dto instanceof ProcessorItemDto processor) {
                saveProcessor(processor);
            } else if (dto instanceof GraphicsCardItemDto gpu) {
                saveGpu(gpu);
            }
            else if (dto instanceof MotherboardItemDto mb) {
                saveMotherboard(mb);
            }
            else if (dto instanceof CaseItemDto c) {
                saveCase(c);
            }
            else if (dto instanceof MemoryItemDto m) {
                saveMemory(m);
            }
            else if (dto instanceof PowerSupplyItemDto p) {
                savePowerSupply(p);
            }
            else if (dto instanceof CoolerItemDto c) {
                saveCooler(c);
            } else if (dto instanceof StorageItemDto s) {
                saveStorage(s);
            }
            else {
                throw new IllegalArgumentException("Nieobsługiwany typ komponentu: " + dto.getComponentType());
            }
        }


    private void saveProcessor(ProcessorItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("Processor must have brand and model");
        }

        Component component = getOrCreateComponent(dto.getBrand(), dto.getModel(), ComponentType.PROCESSOR);

        Processor cpu = component.getProcessor();
        if (cpu == null) {
            cpu = new Processor();
            cpu.setComponent(component);
            component.setProcessor(cpu);
        }

        setIfNotNull(dto.getCores(), cpu::setCores);
        setIfNotNull(dto.getThreads(), cpu::setThreads);
        setIfNotNull(dto.getSocketType(), cpu::setSocketType);
        setIfNotNull(dto.getBaseClock(), cpu::setBaseClock);
        setIfNotNull(dto.getBoostClock(), cpu::setBoostClock);
        setIfNotNull(dto.getIntegratedGraphics(), cpu::setIntegratedGraphics);
        setIfNotNull(dto.getTdp(), cpu::setTdp);
        setIfNotNull(dto.getBenchmark(), cpu::setBenchmark);

        component.setComponentType(ComponentType.PROCESSOR);
        componentRepository.save(component);
    }

    private Optional<String> extractProcessorChipset(String model) {
        if (model == null) return Optional.empty();
        String[] patterns = new String[] {
                "(Intel\\s+Core\\s+i[3579]-?\\d{3,4}[A-Za-z0-9-]*)",    // Intel Core i7-4770K etc.
                "(AMD\\s+FX-\\d{3,4}[A-Za-z0-9-]*)",                    // AMD FX-6300
                "(AMD\\s+Ryzen\\s+\\d\\s*\\d{3,4}[A-Za-z0-9-]*)",       // AMD Ryzen 5 1500X
                "(Intel\\s+Pentium\\s+\\w+)",                           // inne przypadki
        };
        for (String pat : patterns) {
            Pattern p = Pattern.compile(pat, Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(model);
            if (m.find()) {
                return Optional.of(normalize(m.group().replaceAll("\\s+", " ")));
            }
        }
        return Optional.empty();
    }


    @Transactional
    public void saveGpu(GraphicsCardItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("GPU must have brand and model");
        }

        String brand = normalize(dto.getBrand());
        String model = normalize(dto.getModel());
        String combined = normalize(brand + " " + model);

        Optional<GpuModel> found = findByExactChipset(model);

        if (!found.isPresent()) {
            found = findByContainingToken(brand, model);
        }
        if (!found.isPresent()) {
            // 4) try to extract chipset by regex heuristics (e.g. "RTX 3060 Ti")
            Optional<String> extracted = extractChipsetWithRegex(model);
            if (extracted.isPresent()) {
                String chipsetCandidate = extracted.get();
                // spróbuj dopasować extracted jako exact lub containing
                found = findByExactChipset(chipsetCandidate);
                if (!found.isPresent()) {
                    List<GpuModel> candidates = gpuModelRepository.findByChipsetContainingIgnoreCase(chipsetCandidate);
                    if (!candidates.isEmpty()) found = Optional.of(candidates.get(0));
                }

                if (!found.isPresent()) {
                    GpuModel newModel = new GpuModel();
                    newModel.setChipset(chipsetCandidate);
                    newModel = gpuModelRepository.save(newModel);
                    found = Optional.of(newModel);
                }
            }
        }


        GpuModel gpuModel = found.orElse(null);
        if (gpuModel == null) {
//            GpuModel placeholder = new GpuModel();
//            placeholder.setChipset(model);
//            placeholder = gpuModelRepository.save(placeholder);
//            gpuModel = placeholder;
            return;
        }

        Component component = getOrCreateComponent(brand, model, ComponentType.GRAPHICS_CARD);
        GraphicsCard gpu = component.getGraphicsCard();
        if (gpu == null) {
            gpu = new GraphicsCard();
            gpu.setComponent(component);
            component.setGraphicsCard(gpu);
        }

        gpu.setGpuModel(gpuModel);

        setIfNotNull(dto.getVram(), gpu::setVram);
        setIfNotNull(dto.getGddr(), gpu::setGddr);
        setIfNotNull(dto.getPowerDraw(), gpu::setPowerDraw);
        setIfNotNull(dto.getBoostClock(), gpu::setBoostClock);
        setIfNotNull(dto.getCoreClock(), gpu::setCoreClock);
        setIfNotNull(dto.getLengthInMM(), gpu::setLengthInMM);
        setIfNotNull(dto.getBenchmark(), gpu::setBenchmark);

        component.setComponentType(ComponentType.GRAPHICS_CARD);
        componentRepository.save(component);
    }
    private String normalize(String s) {
        if (s == null) return null;
        return s.trim().replaceAll("\\s+", " ");
    }

    private Optional<GpuModel> findByContainingToken(String brand, String model) {
        String combined = (brand == null ? "" : brand + " ") + (model == null ? "" : model);
        combined = normalize(combined);

        // Pobierz wszystkie modele i posortuj po długości desc (dłuższe najpierw)
        List<GpuModel> all = gpuModelRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt((GpuModel gm) -> gm.getChipset().length()).reversed())
                .collect(Collectors.toList());

        for (GpuModel gm : all) {
            String chipset = gm.getChipset();
            if (chipset == null || chipset.isBlank()) continue;
            // tokenowy match: \bCHIPSET\b (Pattern.quote dla bezpieczeństwa)
            Pattern p = Pattern.compile("\\b" + Pattern.quote(chipset) + "\\b", Pattern.CASE_INSENSITIVE);
            if (p.matcher(combined).find()) {
                return Optional.of(gm);
            }
        }
        return Optional.empty();
    }

    private Optional<String> extractChipsetWithRegex(String model) {
        if (model == null) return Optional.empty();
        String[] patterns = new String[] {
                "(RTX|GTX)\\s*\\d{3,4}\\s*(Ti|Super|S)?",        // Nvidia desktop (RTX 3060 Ti, GTX 1660 Super)
                "Radeon\\s*(RX\\s*\\d{3,4})(\\s*XT|\\s*XTX|\\s*XT)?", // AMD Radeon
                "RX\\s*\\d{3,4}\\s*(XT|XTX)?",                  // RX 6700 XT
                "Arc\\s*B5\\d{2}",                              // Intel Arc examples
                "UHD\\s*Graphics\\s*\\d{3}",                    // Intel UHD Graphics 630
                "(Apple)\\s*(M1|M2)\\s*(GPU)?"                  // Apple M1/M2
        };
        for (String pat : patterns) {
            Pattern p = Pattern.compile(pat, Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(model);
            if (m.find()) {
                return Optional.of(normalize(m.group().replaceAll("\\s+", " ")));
            }
        }
        return Optional.empty();
    }

    private Optional<GpuModel> findByExactChipset(String chipset) {
        if (chipset == null || chipset.isBlank()) return Optional.empty();
        return gpuModelRepository.findByChipsetIgnoreCase(chipset.trim());
    }


    private void saveMotherboard(MotherboardItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("Motherboard must have brand and model");
        }

        Component component = getOrCreateComponent(dto.getBrand(), dto.getModel(), ComponentType.MOTHERBOARD);

        Motherboard mb = component.getMotherboard();
        if (mb == null) {
            mb = new Motherboard();
            mb.setComponent(component);
            component.setMotherboard(mb);
        }

        setIfNotNull(dto.getChipset(), mb::setChipset);
        setIfNotNull(dto.getSocketType(), mb::setSocketType);
        setIfNotNull(dto.getFormat(), mb::setFormat);
        setIfNotNull(dto.getRamSlots(), mb::setRamSlots);
        setIfNotNull(dto.getRamCapacity(), mb::setRamCapacity);
        setIfNotNull(dto.getMemoryType(), mb::setMemoryType);

        component.setComponentType(ComponentType.MOTHERBOARD);
        componentRepository.save(component);
    }

    public void saveCase(CaseItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("Case must have brand and model");
        }

        Component component = getOrCreateComponent(dto.getBrand(), dto.getModel(), ComponentType.CASE_PC);

        Case c = component.getCase_();
        if (c == null) {
            c = new Case();
            c.setComponent(component);
            component.setCase_(c);
        }

        setIfNotNull(dto.getFormat(), c::setFormat);

        component.setComponentType(ComponentType.CASE_PC);
        componentRepository.save(component);
    }

    public void saveMemory(MemoryItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("Memory must have brand and model");
        }

        Component component = getOrCreateComponent(dto.getBrand(), dto.getModel(), ComponentType.MEMORY);

        Memory m = component.getMemory();
        if (m == null) {
            m = new Memory();
            m.setComponent(component);
            component.setMemory(m);
        }

        setIfNotNull(dto.getType(), m::setType);
        setIfNotNull(dto.getCapacity(), m::setCapacity);
        setIfNotNull(dto.getSpeed(), m::setSpeed);
        setIfNotNull(dto.getLatency(), m::setLatency);
        setIfNotNull(dto.getAmount(), m::setAmount);

        component.setComponentType(ComponentType.MEMORY);
        componentRepository.save(component);
    }

    public void savePowerSupply(PowerSupplyItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("Power supply must have brand and model");
        }

        Component component = getOrCreateComponent(dto.getBrand(), dto.getModel(), ComponentType.POWER_SUPPLY);

        PowerSupply ps = component.getPowerSupply();
        if (ps == null) {
            ps = new PowerSupply();
            ps.setComponent(component);
            component.setPowerSupply(ps);
        }

        setIfNotNull(dto.getMaxPowerWatt(), ps::setMaxPowerWatt);
        setIfNotNull(dto.getType(), ps::setType);
        setIfNotNull(dto.getModular(), ps::setModular);
        setIfNotNull(dto.getEfficiencyRating(), ps::setEfficiencyRating);

        component.setComponentType(ComponentType.POWER_SUPPLY);
        componentRepository.save(component);
    }

    public void saveCooler(CoolerItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            System.out.println(dto);
            return;
        }

        Component component = getOrCreateComponent(dto.getBrand(), dto.getModel(), ComponentType.CPU_COOLER);

        Cooler c = component.getCooler();
        if (c == null) {
            c = new Cooler();
            c.setComponent(component);
            component.setCooler(c);
        }

        setIfNotNull(dto.getCoolerSocketsType(), c::setSocketTypes);
        setIfNotNull(dto.getFanRpm(), c::setFanRpm);
        setIfNotNull(dto.getNoiseLevel(), c::setNoiseLevel);
        setIfNotNull(dto.getRadiatorSize(), c::setRadiatorSize);

        component.setComponentType(ComponentType.CPU_COOLER);
        componentRepository.save(component);
    }

    public void saveStorage(StorageItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("Storage must have brand and model");
        }

        Component component = getOrCreateComponent(dto.getBrand(), dto.getModel(), ComponentType.STORAGE);

        Storage s = component.getStorage();
        if (s == null) {
            s = new Storage();
            s.setComponent(component);
            component.setStorage(s);
        }

        setIfNotNull(dto.getCapacity(), s::setCapacity);

        component.setComponentType(ComponentType.STORAGE);
        componentRepository.save(component);
    }

    public GameFpsComponentsFormDto getFpsComponents() {

        GameFpsComponentsFormDto formDto = new GameFpsComponentsFormDto();
        formDto.setGpusModels( componentRepository.findAllByComponentType(ComponentType.GRAPHICS_CARD)
                .stream()
                .map(Component::getModel)
                .collect(Collectors.toSet()));

        formDto.setCpusModels(componentRepository.findAllByComponentType(ComponentType.PROCESSOR)
                .stream()
                .map(Component::getModel)
                .collect(Collectors.toSet()));

        return formDto;
    }


    public int amountOfComponents() {
        return (int) componentRepository.count();
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
