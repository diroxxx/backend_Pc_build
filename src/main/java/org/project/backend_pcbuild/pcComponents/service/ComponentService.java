package org.project.backend_pcbuild.pcComponents.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
                    Root<Processor> p = cb.treat(root, Processor.class);
                    predicates.add(cb.like(cb.lower(cb.coalesce(p.get("socketType"), cb.literal(""))), term));
                }
                if (type == null || type == ComponentType.MEMORY) {
                    Root<Memory> m = cb.treat(root, Memory.class);
                    predicates.add(cb.like(cb.lower(cb.coalesce(m.get("type"), cb.literal(""))), term));
                }
                if (type == null || type == ComponentType.GRAPHICS_CARD) {
                    Root<GraphicsCard> g = cb.treat(root, GraphicsCard.class);
                    predicates.add(cb.like(cb.lower(cb.coalesce(g.get("gddr"), cb.literal(""))), term));
                }
                if (type == null || type == ComponentType.MOTHERBOARD) {
                    Root<Motherboard> mb = cb.treat(root, Motherboard.class);
                    predicates.add(cb.like(cb.lower(cb.coalesce(mb.get("chipset"), cb.literal(""))), term));
                }
                if (type == null || type == ComponentType.CASE_PC) {
                    Root<Case> cs = cb.treat(root, Case.class);
                    predicates.add(cb.like(cb.lower(cb.coalesce(cs.get("format"), cb.literal(""))), term));
                }
                if (type == null || type == ComponentType.POWER_SUPPLY) {
                    Root<PowerSupply> ps = cb.treat(root, PowerSupply.class);
                    predicates.add(cb.like(cb.lower(cb.coalesce(ps.get("type"), cb.literal(""))), term));
                }

                return cb.or(predicates.toArray(new Predicate[0]));
            });
        }

        Page<Component> itemsPage = componentRepository.findAll(spec, pageable);
        return itemsPage.map(this::mapToDto);
    }


    private BaseItemDto mapToDto(Component component) {
        return switch (component) {
            case Processor p       -> itemComponentMapper.toDto(p);
            case GraphicsCard g    -> itemComponentMapper.toDto(g);
            case Motherboard mb    -> itemComponentMapper.toDto(mb);
            case Memory m          -> itemComponentMapper.toDto(m);
            case Cooler c          -> itemComponentMapper.toDto(c);
            case PowerSupply ps    -> itemComponentMapper.toDto(ps);
            case Case cs           -> itemComponentMapper.toDto(cs);
            case Storage s         -> itemComponentMapper.toDto(s);
            default                -> null;
        };
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

    private <T extends Component> T getOrCreateSubComponent(String brandName, String model, java.util.function.Supplier<T> constructor) {
        Brand brand = getOrCreateBrand(brandName);
        Optional<Component> existing = componentRepository.findByBrandAndModelIgnoreCase(brand, model);
        if (existing.isPresent()) {
            //noinspection unchecked
            return (T) existing.get();
        }
        T c = constructor.get();
        c.setBrand(brand);
        c.setModel(model);
        return c;
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
        } else if (dto instanceof MotherboardItemDto mb) {
            saveMotherboard(mb);
        } else if (dto instanceof CaseItemDto c) {
            saveCase(c);
        } else if (dto instanceof MemoryItemDto m) {
            saveMemory(m);
        } else if (dto instanceof PowerSupplyItemDto p) {
            savePowerSupply(p);
        } else if (dto instanceof CoolerItemDto c) {
            saveCooler(c);
        } else if (dto instanceof StorageItemDto s) {
            saveStorage(s);
        } else {
            throw new IllegalArgumentException("Nieobsługiwany typ komponentu: " + dto.getComponentType());
        }
    }


    private void saveProcessor(ProcessorItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("Processor must have brand and model");
        }
        Processor cpu = getOrCreateSubComponent(dto.getBrand(), dto.getModel(), Processor::new);
        setIfNotNull(dto.getCores(), cpu::setCores);
        setIfNotNull(dto.getThreads(), cpu::setThreads);
        setIfNotNull(dto.getSocketType(), cpu::setSocketType);
        setIfNotNull(dto.getBaseClock(), cpu::setBaseClock);
        setIfNotNull(dto.getBoostClock(), cpu::setBoostClock);
        setIfNotNull(dto.getIntegratedGraphics(), cpu::setIntegratedGraphics);
        setIfNotNull(dto.getTdp(), cpu::setTdp);
        setIfNotNull(dto.getBenchmark(), cpu::setBenchmark);
        componentRepository.save(cpu);
    }

    @Transactional
    public void saveGpu(GraphicsCardItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("GPU must have brand and model");
        }

        String brand = normalize(dto.getBrand());
        String model = normalize(dto.getModel());

        Optional<GpuModel> found = findByExactChipset(model);
        if (found.isEmpty()) {
            found = findByContainingToken(brand, model);
        }
        if (!found.isPresent()) {
            Optional<String> extracted = extractChipsetWithRegex(model);
            if (extracted.isPresent()) {
                String chipsetCandidate = extracted.get();
                found = findByExactChipset(chipsetCandidate);
                if (found.isEmpty()) {
                    List<GpuModel> candidates = gpuModelRepository.findByChipsetContainingIgnoreCase(chipsetCandidate);
                    if (!candidates.isEmpty()) found = Optional.of(candidates.get(0));
                }
                if (found.isEmpty()) {
                    GpuModel newModel = new GpuModel();
                    newModel.setChipset(chipsetCandidate);
                    newModel = gpuModelRepository.save(newModel);
                    found = Optional.of(newModel);
                }
            }
        }

        GpuModel gpuModel = found.orElse(null);
        if (gpuModel == null) {
            return;
        }

        GraphicsCard gpu = getOrCreateSubComponent(brand, model, GraphicsCard::new);
        gpu.setGpuModel(gpuModel);
        setIfNotNull(dto.getVram(), gpu::setVram);
        setIfNotNull(dto.getGddr(), gpu::setGddr);
        setIfNotNull(dto.getPowerDraw(), gpu::setPowerDraw);
        setIfNotNull(dto.getBoostClock(), gpu::setBoostClock);
        setIfNotNull(dto.getCoreClock(), gpu::setCoreClock);
        setIfNotNull(dto.getLengthInMM(), gpu::setLengthInMM);
        setIfNotNull(dto.getBenchmark(), gpu::setBenchmark);
        componentRepository.save(gpu);
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
        String[] patterns = new String[]{
                "(RTX|GTX)\\s*\\d{3,4}\\s*(Ti|Super|S)?",
                "Radeon\\s*(RX\\s*\\d{3,4})(\\s*XT|\\s*XTX|\\s*XT)?",
                "RX\\s*\\d{3,4}\\s*(XT|XTX)?",
                "Arc\\s*B5\\d{2}",
                "UHD\\s*Graphics\\s*\\d{3}"
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
        Motherboard mb = getOrCreateSubComponent(dto.getBrand(), dto.getModel(), Motherboard::new);
        setIfNotNull(dto.getChipset(), mb::setChipset);
        setIfNotNull(dto.getSocketType(), mb::setSocketType);
        setIfNotNull(dto.getFormat(), mb::setFormat);
        setIfNotNull(dto.getRamSlots(), mb::setRamSlots);
        setIfNotNull(dto.getRamCapacity(), mb::setRamCapacity);
        setIfNotNull(dto.getMemoryType(), mb::setMemoryType);
        componentRepository.save(mb);
    }

    public void saveCase(CaseItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("Case must have brand and model");
        }
        Case c = getOrCreateSubComponent(dto.getBrand(), dto.getModel(), Case::new);
        setIfNotNull(dto.getFormat(), c::setFormat);
        componentRepository.save(c);
    }

    public void saveMemory(MemoryItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("Memory must have brand and model");
        }
        Memory m = getOrCreateSubComponent(dto.getBrand(), dto.getModel(), Memory::new);
        setIfNotNull(dto.getType(), m::setType);
        setIfNotNull(dto.getCapacity(), m::setCapacity);
        setIfNotNull(dto.getSpeed(), m::setSpeed);
        setIfNotNull(dto.getLatency(), m::setLatency);
        setIfNotNull(dto.getAmount(), m::setAmount);
        componentRepository.save(m);
    }

    public void savePowerSupply(PowerSupplyItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("Power supply must have brand and model");
        }
        PowerSupply ps = getOrCreateSubComponent(dto.getBrand(), dto.getModel(), PowerSupply::new);
        setIfNotNull(dto.getMaxPowerWatt(), ps::setMaxPowerWatt);
        setIfNotNull(dto.getType(), ps::setType);
        setIfNotNull(dto.getModular(), ps::setModular);
        setIfNotNull(dto.getEfficiencyRating(), ps::setEfficiencyRating);
        componentRepository.save(ps);
    }

    public void saveCooler(CoolerItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            System.out.println(dto);
            return;
        }
        Cooler c = getOrCreateSubComponent(dto.getBrand(), dto.getModel(), Cooler::new);
        setIfNotNull(dto.getCoolerSocketsType(), c::setSocketTypes);
        setIfNotNull(dto.getFanRpm(), c::setFanRpm);
        setIfNotNull(dto.getNoiseLevel(), c::setNoiseLevel);
        setIfNotNull(dto.getRadiatorSize(), c::setRadiatorSize);
        componentRepository.save(c);
    }

    public void saveStorage(StorageItemDto dto) {
        if (dto.getBrand() == null || dto.getModel() == null) {
            throw new IllegalArgumentException("Storage must have brand and model");
        }
        Storage s = getOrCreateSubComponent(dto.getBrand(), dto.getModel(), Storage::new);
        setIfNotNull(dto.getCapacity(), s::setCapacity);
        componentRepository.save(s);
    }

    public GameFpsComponentsFormDto getFpsComponents() {
        GameFpsComponentsFormDto formDto = new GameFpsComponentsFormDto();
        formDto.setGpusModels(componentRepository.findAllByComponentType(ComponentType.GRAPHICS_CARD)
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
        List<String> models = componentRepository.findProcessorModelsOrderedByBenchmarkDesc();
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
        LocalDateTime thirtyDaysAgo = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        if (useFullTextSearch) {
            return componentRepository.componentStatsPcBetweenMSSQL(thirtyDaysAgo, now);
        }
        return componentRepository.componentStatsPcBetweenH2(thirtyDaysAgo, now);
    }

    @Transactional
    public void updateComponent(Long id, BaseItemDto dto) {
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

        switch (component) {
            case Processor cpu -> {
                ProcessorItemDto p = (ProcessorItemDto) dto;
                setIfNotNull(p.getCores(), cpu::setCores);
                setIfNotNull(p.getThreads(), cpu::setThreads);
                setIfNotNull(p.getSocketType(), cpu::setSocketType);
                setIfNotNull(p.getBaseClock(), cpu::setBaseClock);
                setIfNotNull(p.getBoostClock(), cpu::setBoostClock);
                setIfNotNull(p.getIntegratedGraphics(), cpu::setIntegratedGraphics);
                setIfNotNull(p.getTdp(), cpu::setTdp);
                setIfNotNull(p.getBenchmark(), cpu::setBenchmark);
            }
            case GraphicsCard gpu -> {
                GraphicsCardItemDto g = (GraphicsCardItemDto) dto;
                gpuModelRepository.findByChipsetIgnoreCase(g.getBaseModel()).ifPresent(gpuModel -> {
                    gpu.setGpuModel(gpuModel);
                    setIfNotNull(g.getVram(), gpu::setVram);
                    setIfNotNull(g.getGddr(), gpu::setGddr);
                    setIfNotNull(g.getPowerDraw(), gpu::setPowerDraw);
                    setIfNotNull(g.getBoostClock(), gpu::setBoostClock);
                    setIfNotNull(g.getCoreClock(), gpu::setCoreClock);
                    setIfNotNull(g.getLengthInMM(), gpu::setLengthInMM);
                    setIfNotNull(g.getBenchmark(), gpu::setBenchmark);
                });
            }
            case Motherboard mb -> {
                MotherboardItemDto m = (MotherboardItemDto) dto;
                setIfNotNull(m.getChipset(), mb::setChipset);
                setIfNotNull(m.getSocketType(), mb::setSocketType);
                setIfNotNull(m.getFormat(), mb::setFormat);
                setIfNotNull(m.getRamSlots(), mb::setRamSlots);
                setIfNotNull(m.getRamCapacity(), mb::setRamCapacity);
                setIfNotNull(m.getMemoryType(), mb::setMemoryType);
            }
            case Memory mem -> {
                MemoryItemDto mm = (MemoryItemDto) dto;
                setIfNotNull(mm.getType(), mem::setType);
                setIfNotNull(mm.getCapacity(), mem::setCapacity);
                setIfNotNull(mm.getSpeed(), mem::setSpeed);
                setIfNotNull(mm.getLatency(), mem::setLatency);
                setIfNotNull(mm.getAmount(), mem::setAmount);
            }
            case PowerSupply ps -> {
                PowerSupplyItemDto psDto = (PowerSupplyItemDto) dto;
                setIfNotNull(psDto.getMaxPowerWatt(), ps::setMaxPowerWatt);
                setIfNotNull(psDto.getType(), ps::setType);
                setIfNotNull(psDto.getModular(), ps::setModular);
                setIfNotNull(psDto.getEfficiencyRating(), ps::setEfficiencyRating);
            }
            case Cooler cooler -> {
                CoolerItemDto cDto = (CoolerItemDto) dto;
                setIfNotNull(cDto.getCoolerSocketsType(), cooler::setSocketTypes);
                setIfNotNull(cDto.getFanRpm(), cooler::setFanRpm);
                setIfNotNull(cDto.getNoiseLevel(), cooler::setNoiseLevel);
                setIfNotNull(cDto.getRadiatorSize(), cooler::setRadiatorSize);
            }
            case Case cs -> {
                CaseItemDto cDto = (CaseItemDto) dto;
                setIfNotNull(cDto.getFormat(), cs::setFormat);
            }
            case Storage st -> {
                StorageItemDto sDto = (StorageItemDto) dto;
                setIfNotNull(sDto.getCapacity(), st::setCapacity);
            }
            default -> throw new IllegalArgumentException("Unsupported component type: " + component.getClass());
        }

        componentRepository.save(component);
    }

    @Transactional
    public void deleteComponent(Long id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        componentRepository.deleteById(id);
    }

}