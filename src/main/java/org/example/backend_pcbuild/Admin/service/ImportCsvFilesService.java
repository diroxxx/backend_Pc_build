package org.example.backend_pcbuild.Admin.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_pcbuild.Component.service.ComponentService;
import org.example.backend_pcbuild.Component.dto.*;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.ComponentRepository;
import org.example.backend_pcbuild.repository.GameRepository;
import org.example.backend_pcbuild.repository.GpuModelRepository;
import org.example.backend_pcbuild.repository.ProcessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportCsvFilesService {

    private final ComponentService componentService;
    private final GameRepository gameRepository;
    private final GpuModelRepository gpuModelRepository;
    private final ComponentRepository componentRepository;
    private final ProcessorRepository processorRepository;


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
        // ładowanie katalogu (możesz zmienić na query w razie potrzeby)
        List<GpuModel> models = gpuModelRepository.findAll();
        List<Processor> processors = processorRepository.findAll();

        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<Game> games = prepareCsvToParse(reader, Game.class);

            for (Game game : games) {
                game.decodeImageFromBase64();

                // CPU - minimal
                Optional<Processor> minCpuOpt = findProcessorFromGameField(game.getMinCpu(), processors);
                if (minCpuOpt.isPresent()) {
                    Processor minCpu = minCpuOpt.get();
                    GameCpuRequirements reqMinCpu = new GameCpuRequirements();
                    reqMinCpu.setProcessor(minCpu);
                    reqMinCpu.setRecGameLevel(RecGameLevel.MIN);
                    reqMinCpu.setGame(game);
                    game.getGameCpuRequirements().add(reqMinCpu);
                } else {
                    log.warn("Nie znaleziono CPU (MIN) dla gry {}: '{}'", game.getTitle(), game.getMinCpu());
                }

                // CPU - recommended
                Optional<Processor> recCpuOpt = findProcessorFromGameField(game.getRecCpu(), processors);
                if (recCpuOpt.isPresent()) {
                    Processor recCpu = recCpuOpt.get();
                    // unikamy duplikatu (gdy min==rec znalezione ten sam procesor)
                    boolean duplicate = game.getGameCpuRequirements().stream()
                            .anyMatch(r -> r.getProcessor() != null && r.getProcessor().equals(recCpu) && r.getRecGameLevel() == RecGameLevel.MIN);
                    if (!duplicate) {
                        GameCpuRequirements reqRecCpu = new GameCpuRequirements();
                        reqRecCpu.setProcessor(recCpu);
                        reqRecCpu.setRecGameLevel(RecGameLevel.REC);
                        reqRecCpu.setGame(game);
                        game.getGameCpuRequirements().add(reqRecCpu);
                    }
                } else {
                    log.warn("Nie znaleziono CPU (REC) dla gry {}: '{}'", game.getTitle(), game.getRecCpu());
                }

                // GPU - minimal
                Optional<GpuModel> minGpuOpt = findGpuModelFromGameField(game.getMinGpu(), models);
                if (minGpuOpt.isPresent()) {
                    GpuModel minGpu = minGpuOpt.get();
                    GameGpuRequirements reqMinGpu = new GameGpuRequirements();
                    reqMinGpu.setGpuModel(minGpu);
                    reqMinGpu.setRecGameLevel(RecGameLevel.MIN);
                    reqMinGpu.setGame(game);
                    game.getGameGpuRequirements().add(reqMinGpu);
                } else {
                    log.warn("Nie znaleziono GPU (MIN) dla gry {}: '{}'", game.getTitle(), game.getMinGpu());
                }

                // GPU - recommended
                Optional<GpuModel> recGpuOpt = findGpuModelFromGameField(game.getRecGpu(), models);
                if (recGpuOpt.isPresent()) {
                    GpuModel recGpu = recGpuOpt.get();
                    boolean duplicate = game.getGameGpuRequirements().stream()
                            .anyMatch(r -> r.getGpuModel() != null && r.getGpuModel().equals(recGpu) && r.getRecGameLevel() == RecGameLevel.MIN);
                    if (!duplicate) {
                        GameGpuRequirements reqRecGpu = new GameGpuRequirements();
                        reqRecGpu.setGpuModel(recGpu);
                        reqRecGpu.setRecGameLevel(RecGameLevel.REC);
                        reqRecGpu.setGame(game);
                        game.getGameGpuRequirements().add(reqRecGpu);
                    }
                } else {
                    log.warn("Nie znaleziono GPU (REC) dla gry {}: '{}'", game.getTitle(), game.getRecGpu());
                }
            }

            gameRepository.saveAll(games);
            return games.size();
        }
    }
    private Optional<Processor> findProcessorFromGameField(String gameField, List<Processor> allProcessors) {
        if (gameField == null || gameField.isBlank()) return Optional.empty();
        List<String> tokens = splitAlternatives(gameField);
        for (String token : tokens) {
            String t = normalize(token);
            // najpierw direct contain on component.model (case-insensitive)
            Optional<Processor> found = allProcessors.stream()
                    .filter(p -> {
                        String compModel = p.getComponent() != null && p.getComponent().getModel() != null
                                ? p.getComponent().getModel().toLowerCase() : "";
                        return compModel.contains(t);
                    })
                    .findFirst();
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    private Optional<GpuModel> findGpuModelFromGameField(String gameField, List<GpuModel> allModels) {
        if (gameField == null || gameField.isBlank()) return Optional.empty();
        List<String> tokens = splitAlternatives(gameField);
        for (String token : tokens) {
            String t = normalize(token);
            Optional<GpuModel> found = allModels.stream()
                    .filter(m -> m.getChipset() != null && m.getChipset().toLowerCase().contains(t))
                    .findFirst();
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    private List<String> splitAlternatives(String s) {
        // rozdziela " / ", "/", " or ", ",", " | "
        return Arrays.stream(s.split("\\s*(/|,|\\bor\\b|\\|)\\s*"))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList());
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase().replaceAll("\\s+", " ");
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
