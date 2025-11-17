package org.example.backend_pcbuild.Admin.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Component.ComponentService;
import org.example.backend_pcbuild.Component.dto.*;
import org.example.backend_pcbuild.models.ComponentType;
import org.example.backend_pcbuild.repository.ComponentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportCsvFilesService {

    private final ComponentService componentService;


    private <T extends BaseItemDto> List<T> parseCsv(Reader reader, Class<T> type) {
        HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(type);

        CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                .withMappingStrategy(strategy)
                .withIgnoreEmptyLine(true)
                .withIgnoreLeadingWhiteSpace(true)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .build();

        return csvToBean.parse();
    }

    private <T extends BaseItemDto> List<T> parseCsv(MultipartFile file, Class<T> type) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return parseCsv(reader, type);
        }
    }

    public Integer importComponentsFromCsv(MultipartFile file, ComponentType componentType) throws IOException {
        List<? extends BaseItemDto> items;

        switch (componentType) {
            case PROCESSOR -> {
                items = parseCsv(file, ProcessorItemDto.class);
            }
            case GRAPHICS_CARD -> {
                items = parseCsv(file, GraphicsCardItemDto.class);
            }
            case MOTHERBOARD -> {
                items = parseCsv(file, MotherboardItemDto.class);
            }
            case MEMORY -> {
                items = parseCsv(file, MemoryItemDto.class);
            }
            case POWER_SUPPLY -> {
                items = parseCsv(file, PowerSupplyItemDto.class);
            }
            case CPU_COOLER -> {
                items = parseCsv(file, CoolerItemDto.class);
            }
            case CASE_PC -> {
                items = parseCsv(file, CaseItemDto.class);
            }
            case STORAGE -> {
                items = parseCsv(file, StorageItemDto.class);
            }
            default -> throw new IllegalArgumentException("Nieobsługiwany typ komponentu: " + componentType);
        }

        componentService.saveComponents(items);
        return items.size();
    }

    public Integer importComponentsFromCsv(InputStream inputStream, ComponentType componentType) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<? extends BaseItemDto> items;

            switch (componentType) {
                case PROCESSOR -> {
                    items = parseCsv(reader, ProcessorItemDto.class);
                }
                case GRAPHICS_CARD -> {
                    items = parseCsv(reader, GraphicsCardItemDto.class);
                }
                case MOTHERBOARD -> {
                    items = parseCsv(reader, MotherboardItemDto.class);
                }
                case MEMORY -> {
                    items = parseCsv(reader, MemoryItemDto.class);
                }
                case POWER_SUPPLY -> {
                    items = parseCsv(reader, PowerSupplyItemDto.class);
                }
                case CPU_COOLER -> {
                    items = parseCsv(reader, CoolerItemDto.class);
                }
                case CASE_PC -> {
                    items = parseCsv(reader, CaseItemDto.class);
                }
                case STORAGE -> {
                    items = parseCsv(reader, StorageItemDto.class);
                }
                default -> throw new IllegalArgumentException("Nieobsługiwany typ komponentu: " + componentType);
            }

            componentService.saveComponents(items);
            return items.size();
        }
    }
}
