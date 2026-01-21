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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentService {

    private final ComponentRepository componentRepository;
    private final ItemComponentMapper itemComponentMapper;
    private final BrandRepository brandRepository;
    private final GpuModelRepository gpuModelRepository;

    
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
            Optional<String> extracted = extractChipsetWithRegex(model);
            if (extracted.isPresent()) {
                String chipsetCandidate = extracted.get();

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

        List<GpuModel> all = gpuModelRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt((GpuModel gm) -> gm.getChipset().length()).reversed())
                .collect(Collectors.toList());

        for (GpuModel gm : all) {
            String chipset = gm.getChipset();
            if (chipset == null || chipset.isBlank()) continue;
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


    public Set<String> getCpus() {
        List<String> models = componentRepository.findProcessorModelsOrderedByBenchmarkDesc(ComponentType.PROCESSOR);
        return new LinkedHashSet<>(models);
    }

    public Set<String> getGpusModels() {

        return gpuModelRepository.findAll()
                .stream()
                .map(GpuModel::getChipset)
                .collect(Collectors.toSet());
    }


    public int amountOfComponents() {
        return (int) componentRepository.count();
    }


    @Value("${app.search.useFullTextSearch:false}")
    private boolean useFullTextSearch;
    public List<ComponentsAmountPc> getComponentsPcStats() {

        LocalDateTime thirtyDaysAgo = LocalDate.now()
                .minusDays(30)
                .atStartOfDay();

        LocalDateTime now = LocalDateTime.now();

        if (useFullTextSearch) {
            return componentRepository.componentStatsPcBetweenMSSQL(
                    thirtyDaysAgo,
                    now
            );
        }

        return componentRepository.componentStatsPcBetweenH2(
                thirtyDaysAgo,
                now
        );
        }


    @Transactional
    public void updateComponent(Integer id, BaseItemDto dto) {
        log.info("Updating component {}: {}", id, dto);
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Component not found: " + id));

        if (dto.getBrand() != null && !dto.getBrand().isBlank()) {
            component.setBrand(getOrCreateBrand(dto.getBrand()));
        }
        if (dto.getModel() != null && !dto.getModel().isBlank()) {
            component.setModel(dto.getModel());
        }

        if (dto.getComponentType() == null) {
            throw new IllegalArgumentException("Component type required for update");
        }
        component.setComponentType(dto.getComponentType());

        switch (dto.getComponentType()) {
            case PROCESSOR -> {
                ProcessorItemDto p = (ProcessorItemDto) dto;
                Processor cpu = component.getProcessor();
                if (cpu == null) {
                    cpu = new Processor();
                    cpu.setComponent(component);
                    component.setProcessor(cpu);
                }
                setIfNotNull(p.getCores(), cpu::setCores);
                setIfNotNull(p.getThreads(), cpu::setThreads);
                setIfNotNull(p.getSocketType(), cpu::setSocketType);
                setIfNotNull(p.getBaseClock(), cpu::setBaseClock);
                setIfNotNull(p.getBoostClock(), cpu::setBoostClock);
                setIfNotNull(p.getIntegratedGraphics(), cpu::setIntegratedGraphics);
                setIfNotNull(p.getTdp(), cpu::setTdp);
                setIfNotNull(p.getBenchmark(), cpu::setBenchmark);
                component.setComponentType(ComponentType.PROCESSOR);
            }
            case GRAPHICS_CARD -> {
                GraphicsCardItemDto g = (GraphicsCardItemDto) dto;
                GraphicsCard gpu = component.getGraphicsCard();
                if (gpu == null) {
                    gpu = new GraphicsCard();
                    gpu.setComponent(component);
                    component.setGraphicsCard(gpu);
                }

                Optional<GpuModel> byChipsetIgnoreCase = gpuModelRepository.findByChipsetIgnoreCase(g.getBaseModel());
                if (byChipsetIgnoreCase.isPresent()) {
                    gpu.setGpuModel(byChipsetIgnoreCase.get());
                    setIfNotNull(g.getVram(), gpu::setVram);
                    setIfNotNull(g.getGddr(), gpu::setGddr);
                    setIfNotNull(g.getPowerDraw(), gpu::setPowerDraw);
                    setIfNotNull(g.getBoostClock(), gpu::setBoostClock);
                    setIfNotNull(g.getCoreClock(), gpu::setCoreClock);
                    setIfNotNull(g.getLengthInMM(), gpu::setLengthInMM);
                    setIfNotNull(g.getBenchmark(), gpu::setBenchmark);
                }
                component.setComponentType(ComponentType.GRAPHICS_CARD);

            }
            case MOTHERBOARD -> {
                MotherboardItemDto m = (MotherboardItemDto) dto;
                Motherboard mb = component.getMotherboard();
                if (mb == null) {
                    mb = new Motherboard();
                    mb.setComponent(component);
                    component.setMotherboard(mb);
                }
                setIfNotNull(m.getChipset(), mb::setChipset);
                setIfNotNull(m.getSocketType(), mb::setSocketType);
                setIfNotNull(m.getFormat(), mb::setFormat);
                setIfNotNull(m.getRamSlots(), mb::setRamSlots);
                setIfNotNull(m.getRamCapacity(), mb::setRamCapacity);
                setIfNotNull(m.getMemoryType(), mb::setMemoryType);
                component.setComponentType(ComponentType.MOTHERBOARD);
            }
            case MEMORY -> {
                MemoryItemDto mm = (MemoryItemDto) dto;
                Memory mem = component.getMemory();
                if (mem == null) {
                    mem = new Memory();
                    mem.setComponent(component);
                    component.setMemory(mem);
                }
                setIfNotNull(mm.getType(), mem::setType);
                setIfNotNull(mm.getCapacity(), mem::setCapacity);
                setIfNotNull(mm.getSpeed(), mem::setSpeed);
                setIfNotNull(mm.getLatency(), mem::setLatency);
                setIfNotNull(mm.getAmount(), mem::setAmount);
                component.setComponentType(ComponentType.MEMORY);
            }
            case POWER_SUPPLY -> {
                PowerSupplyItemDto psDto = (PowerSupplyItemDto) dto;
                PowerSupply ps = component.getPowerSupply();
                if (ps == null) {
                    ps = new PowerSupply();
                    ps.setComponent(component);
                    component.setPowerSupply(ps);
                }
                setIfNotNull(psDto.getMaxPowerWatt(), ps::setMaxPowerWatt);
                setIfNotNull(psDto.getType(), ps::setType);
                setIfNotNull(psDto.getModular(), ps::setModular);
                setIfNotNull(psDto.getEfficiencyRating(), ps::setEfficiencyRating);
                component.setComponentType(ComponentType.POWER_SUPPLY);
            }
            case CPU_COOLER -> {
                CoolerItemDto cDto = (CoolerItemDto) dto;
                Cooler cooler = component.getCooler();
                if (cooler == null) {
                    cooler = new Cooler();
                    cooler.setComponent(component);
                    component.setCooler(cooler);
                }
                setIfNotNull(cDto.getCoolerSocketsType(), cooler::setSocketTypes);
                setIfNotNull(cDto.getFanRpm(), cooler::setFanRpm);
                setIfNotNull(cDto.getNoiseLevel(), cooler::setNoiseLevel);
                setIfNotNull(cDto.getRadiatorSize(), cooler::setRadiatorSize);
                component.setComponentType(ComponentType.CPU_COOLER);
            }
            case CASE_PC -> {
                Case caseDto = component.getCase_();
                CaseItemDto cDto = (CaseItemDto) dto;
                if (caseDto == null) {
                    caseDto = new Case();
                    caseDto.setComponent(component);
                    component.setCase_(caseDto);
                }
                setIfNotNull(cDto.getFormat(), caseDto::setFormat);
                component.setComponentType(ComponentType.CASE_PC);
            }
            case STORAGE -> {
                StorageItemDto sDto = (StorageItemDto) dto;
                Storage st = component.getStorage();
                if (st == null) {
                    st = new Storage();
                    st.setComponent(component);
                    component.setStorage(st);
                }
                setIfNotNull(sDto.getCapacity(), st::setCapacity);
                component.setComponentType(ComponentType.STORAGE);
            }
            default -> throw new IllegalArgumentException("Unsupported component type: " + dto.getComponentType());
        }

        componentRepository.save(component);
    }

    @Transactional
    public void deleteComponent(Integer id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        componentRepository.deleteById(id);
    }


}
