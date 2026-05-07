package org.project.backend_pcbuild.pcComponents.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.project.backend_pcbuild.pcComponents.dto.ComponentMinMaxValueDto;
import org.project.backend_pcbuild.pcComponents.dto.MonthlyPrice;
import org.project.backend_pcbuild.pcComponents.dto.ProcessorItemDto;
import org.project.backend_pcbuild.pcComponents.dto.ShopPriceHistory;
import org.project.backend_pcbuild.pcComponents.repository.ComponentRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComponentStatsService {
    private final ComponentRepository componentRepository;

    public ComponentMinMaxValueDto getMinMaxValueDto(Long id) {

        return componentRepository.findMinMaxValueDtoById(id);
    }

    public List<ShopPriceHistory> getPriceHistory(Long id) {
        return componentRepository.findMonthlyPricesByComponent(id)
                .stream()
                .collect(Collectors.groupingBy(
                        ComponentRepository.ShopPriceFlat::getName,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                row -> new MonthlyPrice(row.getMonth(), row.getAvgPrice()),
                                Collectors.toList()
                        )
                ))
                .entrySet().stream()
                .map(entry -> new ShopPriceHistory(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
