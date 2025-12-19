package org.project.backend_pcbuild.configuration;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.backend_pcbuild.Game.model.Game;
import org.project.backend_pcbuild.pcComponents.dto.*;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;
import org.project.backend_pcbuild.pcComponents.model.GpuModel;
import org.project.backend_pcbuild.pcComponents.service.ComponentService;
import org.project.backend_pcbuild.pcComponents.dto.*;
import org.project.backend_pcbuild.Game.repository.GameRepository;
import org.project.backend_pcbuild.pcComponents.repository.GpuModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportCsvFilesService {

    private final ComponentService componentService;
    private final GameRepository gameRepository;
    private final GpuModelRepository gpuModelRepository;

    private <T> List<T> prepareCsvToParse(Reader reader, Class<T> type) {
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

    private <T extends BaseItemDto> List<T> prepareComponentsCsvtoParse(Reader reader, Class<T> type) {
        return prepareCsvToParse(reader, type);
    }

    private <T extends BaseItemDto> List<T> prepareComponentsCsvtoParse(MultipartFile file, Class<T> type) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return prepareComponentsCsvtoParse(reader, type);
        }
    }

    @Transactional
    public Integer importComponentsFromCsv(MultipartFile file, ComponentType componentType) throws IOException {
        List<? extends BaseItemDto> items;

        switch (componentType) {
            case PROCESSOR -> {
                items = prepareComponentsCsvtoParse(file, ProcessorItemDto.class);

            }
            case GRAPHICS_CARD -> {
                items = prepareComponentsCsvtoParse(file, GraphicsCardItemDto.class);
            }
            case MOTHERBOARD -> {
                items = prepareComponentsCsvtoParse(file, MotherboardItemDto.class);
            }
            case MEMORY -> {
                items = prepareComponentsCsvtoParse(file, MemoryItemDto.class);
            }
            case POWER_SUPPLY -> {
                items = prepareComponentsCsvtoParse(file, PowerSupplyItemDto.class);
            }
            case CPU_COOLER -> {
                items = prepareComponentsCsvtoParse(file, CoolerItemDto.class);
            }
            case CASE_PC -> {
                items = prepareComponentsCsvtoParse(file, CaseItemDto.class);
            }
            case STORAGE -> {
                items = prepareComponentsCsvtoParse(file, StorageItemDto.class);
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
                    items = prepareComponentsCsvtoParse(reader, ProcessorItemDto.class);
                }
                case GRAPHICS_CARD -> {
                    items = prepareComponentsCsvtoParse(reader, GraphicsCardItemDto.class);
                }
                case MOTHERBOARD -> {
                    items = prepareComponentsCsvtoParse(reader, MotherboardItemDto.class);
                }
                case MEMORY -> {
                    items = prepareComponentsCsvtoParse(reader, MemoryItemDto.class);
                }
                case POWER_SUPPLY -> {
                    items = prepareComponentsCsvtoParse(reader, PowerSupplyItemDto.class);
                }
                case CPU_COOLER -> {
                    items = prepareComponentsCsvtoParse(reader, CoolerItemDto.class);
                }
                case CASE_PC -> {
                    items = prepareComponentsCsvtoParse(reader, CaseItemDto.class);
                }
                case STORAGE -> {
                    items = prepareComponentsCsvtoParse(reader, StorageItemDto.class);
                }
                default -> throw new IllegalArgumentException("Nieobsługiwany typ komponentu: " + componentType);
            }

            componentService.saveComponents(items);
            return items.size();
        }
    }
    @Transactional
    public Integer importGamesFromCsv(InputStream inputStream) throws IOException {

        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<Game> games = prepareCsvToParse(reader, Game.class);
            for (Game game : games) {
                game.decodeImageFromBase64();
            }

            gameRepository.saveAll(games);
            return games.size();
        }
    }

    @Transactional
    public Integer importGpuModelsFromCsv(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<GpuModel> models = prepareCsvToParse(reader, GpuModel.class);

            gpuModelRepository.saveAll(models);
            return models.size();
        }
    }
}
