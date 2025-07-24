package org.example.backend_pcbuild.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.repository.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
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

    private final RestClient restClient = RestClient.create();


    public Map<String, List<Object>> fetchComponentsAsMap() {
        return restClient.get()
                .uri("http://127.0.0.1:5000/components")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }



}
