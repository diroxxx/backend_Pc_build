package org.example.backend_pcbuild.Component;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_pcbuild.Component.dto.*;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComponentService {
    private final RestClient restClient = RestClient.create();
    private final ItemRepository itemRepository;
    private final ItemComponentMapper itemComponentMapper;

    public Map<String, List<Object>> fetchComponentsAsMap() {
        return restClient.get()
                .uri("http://127.0.0.1:5000/installComponents")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<BaseItemDto> getAllComponents() {
        List<Item> items = itemRepository.findAll();

        return items.stream()
                .map(this::mapToDto)
                .filter(Objects::nonNull)
                .toList();
    }

    public Page<BaseItemDto> getComponents(Pageable pageable, ComponentType type, String brand, String searchTerm) {
        Specification<Item> spec = Specification.not(null);

        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("componentType")), "%" + type.name().toLowerCase() + "%"));
        }
        if (brand != null && !brand.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("brand")), "%" + brand.toLowerCase() + "%"));
        }
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String term = "%" + searchTerm.toLowerCase() + "%";

            spec = spec.and((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.like(cb.lower(root.get("brand")), term));
                predicates.add(cb.like(cb.lower(root.get("model")), term));

                Join<Item, Processor> processorJoin = root.join("processor", JoinType.LEFT);
                Join<Item, Memory> memoryJoin = root.join("memory", JoinType.LEFT);
                Join<Item, GraphicsCard> gpuJoin = root.join("graphicsCard", JoinType.LEFT);

                predicates.add(cb.like(cb.lower(processorJoin.get("socket_type")), term));
                predicates.add(cb.like(cb.lower(memoryJoin.get("type")), term));
                predicates.add(cb.like(cb.lower(gpuJoin.get("gddr")), term));
                //to implement rest of components
                return cb.or(predicates.toArray(new Predicate[0]));
            });
        }

        Page<Item> itemsPage = itemRepository.findAll(spec, pageable);

        return itemsPage.map(this::mapToDto);
    }


    private BaseItemDto mapToDto(Item item) {
        if (item == null || item.getComponentType() == null) return null;

        switch (item.getComponentType()) {
            case PROCESSOR -> {
                return itemComponentMapper.toDto(item.getProcessor());
            }
            case GRAPHICS_CARD -> {
                return itemComponentMapper.toDto(item.getGraphicsCard());
            }
            case MOTHERBOARD -> {
                return itemComponentMapper.toDto(item.getMotherboard());
            }
            case MEMORY -> {
                return itemComponentMapper.toDto(item.getMemory());
            }
            case CPU_COOLER -> {
                return itemComponentMapper.toDto(item.getCooler());
            }
            case POWER_SUPPLY -> {
                return itemComponentMapper.toDto(item.getPowerSupply());
            }
            case CASE_PC -> {
                return itemComponentMapper.toDto(item.getCase_());
            }
            case STORAGE -> {
                return itemComponentMapper.toDto(item.getStorage());
            }
            default -> {
                return null;
            }
        }
    }

    public List<String> getAllBrands() {
        return itemRepository.findDistinctBrands();


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

                    Item item = itemRepository.findByBrandAndModel(brand, model)
                            .orElseGet(() -> {
                                Item it = new Item();
                                it.setBrand(brand);
                                it.setModel(model);
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
                            item.setComponentType(ComponentType.PROCESSOR);
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
                            item.setComponentType(ComponentType.STORAGE);
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
                            item.setComponentType(ComponentType.MOTHERBOARD);
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
                            item.setComponentType(ComponentType.POWER_SUPPLY);
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
                            item.setComponentType(ComponentType.CPU_COOLER);
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
                            item.setComponentType(ComponentType.GRAPHICS_CARD);
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
                            item.setComponentType(ComponentType.CASE_PC);
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
                            item.setComponentType(ComponentType.MEMORY);
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
            }
            else {
                throw new IllegalArgumentException("Nieobsługiwany typ komponentu: " + dto.getComponentType());
            }
        }


    private void saveProcessor(ProcessorItemDto dto) {
        Processor cpu = new Processor();
        Item item = new Item();
        item.setBrand(dto.getBrand());
        item.setModel(dto.getModel());
        item.setComponentType(ComponentType.PROCESSOR);
        cpu.setCores(dto.getCores());
        cpu.setThreads(dto.getThreads());
        cpu.setBase_clock(dto.getBaseClock());
        cpu.setSocket_type(dto.getSocketType());
        cpu.setItem(item);
        item.setProcessor(cpu);
        itemRepository.save(item);
    }

    private void saveGpu(GraphicsCardItemDto dto) {
        GraphicsCard gpu = new GraphicsCard();
        Item item = new Item();
        item.setBrand(dto.getBrand());
        item.setModel(dto.getModel());
        gpu.setVram(dto.getVram());
        gpu.setGddr(dto.getGddr());
        gpu.setPower_draw(dto.getPowerDraw());
        item.setComponentType(ComponentType.GRAPHICS_CARD);
        gpu.setItem(item);
        item.setGraphicsCard(gpu);
        itemRepository.save(item);
    }

    private void saveMotherboard(MotherboardItemDto dto) {
        Motherboard mb = new Motherboard();
        Item item = new Item();
        item.setBrand(dto.getBrand());
        item.setModel(dto.getModel());
        mb.setChipset(dto.getChipset());
        mb.setFormat(dto.getFormat());
        mb.setMemoryType(dto.getMemoryType());
        mb.setSocketType(dto.getSocketType());
        mb.setRamSlots(dto.getRamSlots());
        mb.setRamCapacity(dto.getRamCapacity());
        item.setComponentType(ComponentType.MOTHERBOARD);
        mb.setItem(item);
        item.setMotherboard(mb);
        itemRepository.save(item);
    }

    public void saveCase(CaseItemDto dto) {
        Case c = new Case();
        Item item = new Item();
        item.setBrand(dto.getBrand());
        item.setModel(dto.getModel());
        c.setFormat(dto.getFormat());
        item.setComponentType(ComponentType.CASE_PC);
        c.setItem(item);
        item.setCase_(c);
        itemRepository.save(item);
    }

    public void saveMemory(MemoryItemDto dto) {
        Memory m = new Memory();
        Item item = new Item();
        item.setBrand(dto.getBrand());
        item.setModel(dto.getModel());
        m.setCapacity(dto.getCapacity());
        m.setType(dto.getType());
        m.setSpeed(dto.getSpeed());
        m.setLatency(dto.getLatency());
        item.setComponentType(ComponentType.MEMORY);
        m.setItem(item);
        item.setMemory(m);
        itemRepository.save(item);
    }

    public void savePowerSupply(PowerSupplyItemDto dto) {
        PowerSupply ps = new PowerSupply();
        Item item = new Item();
        item.setBrand(dto.getBrand());
        item.setModel(dto.getModel());
        ps.setMaxPowerWatt(dto.getMaxPowerWatt());
        item.setComponentType(ComponentType.POWER_SUPPLY);
        ps.setItem(item);
        item.setPowerSupply(ps);
        itemRepository.save(item);
    }

    public void saveCooler(CoolerItemDto dto) {
        Cooler c = new Cooler();
        Item item = new Item();
        item.setBrand(dto.getBrand());
        item.setModel(dto.getModel());
        item.setComponentType(ComponentType.CPU_COOLER);
        c.setSocketTypes(dto.getCoolerSocketsType());
        c.setItem(item);
        item.setCooler(c);
        itemRepository.save(item);
    }

    public void saveStorage(StorageItemDto dto) {
        Storage s = new Storage();
        Item item = new Item();
        item.setBrand(dto.getBrand());
        item.setModel(dto.getModel());
        s.setCapacity(dto.getCapacity());
        item.setComponentType(ComponentType.STORAGE);
        s.setItem(item);
        item.setStorage(s);
        itemRepository.save(item);
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
