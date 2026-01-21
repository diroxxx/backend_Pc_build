package org.project.backend_pcbuild.Game.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.project.backend_pcbuild.Game.dto.*;
import org.project.backend_pcbuild.Game.dto.*;
import org.project.backend_pcbuild.Game.model.Game;
import org.project.backend_pcbuild.Game.model.GameCpuRequirements;
import org.project.backend_pcbuild.Game.model.GameGpuRequirements;
import org.project.backend_pcbuild.Game.repository.GameCpuRequirementsRepository;
import org.project.backend_pcbuild.Game.repository.GameGpuRequirementsRepository;
import org.project.backend_pcbuild.Game.repository.GameRepository;
import org.project.backend_pcbuild.pcComponents.model.GpuModel;
import org.project.backend_pcbuild.pcComponents.model.Processor;
import org.project.backend_pcbuild.pcComponents.repository.GpuModelRepository;
import org.project.backend_pcbuild.pcComponents.repository.ProcessorRepository;
import org.project.backend_pcbuild.pcComponents.service.ComponentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final GpuModelRepository gpuModelRepository;
    private final ProcessorRepository processorRepository;
    private final GameGpuRequirementsRepository gameGpuRequirementsRepository;
    private final GameCpuRequirementsRepository gameCpuRequirementsRepository;


    public Game findByTitle(String title) {
        Optional<Game> byTitle = gameRepository.findByTitle(title);
        return byTitle.orElse(null);
    }


    public List<GameDto> getAllGames(){
        List<Game> games = gameRepository.findAll();
        return games.stream().map(this::convertToDTO).toList();

    }
    private GameDto convertToDTO(Game game) {
        GameDto dto = new GameDto();

        dto.setId(game.getId());
        dto.setTitle(game.getTitle());
        dto.setImageUrl("/api/games/" + game.getId() + "/image");

        return dto;
    }

    public List<GameReqCompDto> getAllSpecsOfGames() {
        List<GameReqCompDto> dtos = new ArrayList<>();

        List<Game> all = gameRepository.findAll();

        for (Game game : all) {
            List<Processor> listMinCpu = game.getGameCpuRequirements().stream()
                    .map(GameCpuRequirements::getProcessor)
                    .toList();

            List<Processor> listRecCpu = game.getGameCpuRequirements().stream()
                    .filter(gameCpuRequirements -> gameCpuRequirements.getRecGameLevel() == RecGameLevel.REC)
                    .map(GameCpuRequirements::getProcessor)
                    .toList();

            List<GpuModel> listMinGpu = game.getGameGpuRequirements().stream()
                    .filter(gameGpuRequirements -> gameGpuRequirements.getRecGameLevel() == RecGameLevel.MIN)
                    .map(GameGpuRequirements::getGpuModel)
                    .toList();


            List<GpuModel> listRecGpu = game.getGameGpuRequirements().stream()
                    .filter(gameGpuRequirements -> gameGpuRequirements.getRecGameLevel() == RecGameLevel.REC)
                    .map(GameGpuRequirements::getGpuModel)
                    .toList();

            GameReqCompDto gameReqCompDto = new GameReqCompDto();
            gameReqCompDto.setId(game.getId());
            gameReqCompDto.setTitle(game.getTitle());
            gameReqCompDto.setImageUrl("/api/games/" + game.getId() + "/image");

            List<CpuRecDto> cpuSpecs = new ArrayList<>();
            List<GpuRecDto> gpuSpecs = new ArrayList<>();

            listMinCpu.forEach(processor -> {
                CpuRecDto cpuRecDto = new CpuRecDto();
                cpuRecDto.setProcessorId(processor.getId());
                cpuRecDto.setRecGameLevel(RecGameLevel.MIN);
                cpuRecDto.setProcessorModel(processor.getComponent().getModel());
                cpuSpecs.add(cpuRecDto);
            });

            listRecCpu.forEach(processor -> {
                CpuRecDto cpuRecDto = new CpuRecDto();
                cpuRecDto.setProcessorId(processor.getId());
                cpuRecDto.setRecGameLevel(RecGameLevel.REC);
                cpuRecDto.setProcessorModel(processor.getComponent().getModel());
                cpuSpecs.add(cpuRecDto);
            });


            listMinGpu.forEach(gpu -> {
                GpuRecDto gpuRecDto = new GpuRecDto();
                gpuRecDto.setGpuModelId(gpu.getId());
                gpuRecDto.setGpuModel(gpu.getChipset());
                gpuRecDto.setRecGameLevel(RecGameLevel.MIN);
                gpuSpecs.add(gpuRecDto);
            });

            listRecGpu.forEach(gpu -> {
                GpuRecDto gpuRecDto = new GpuRecDto();
                gpuRecDto.setGpuModelId(gpu.getId());
                gpuRecDto.setGpuModel(gpu.getChipset());
                gpuRecDto.setRecGameLevel(RecGameLevel.REC);
                gpuSpecs.add(gpuRecDto);
            });

            gameReqCompDto.setCpuSpecs(cpuSpecs);
            gameReqCompDto.setGpuSpecs(gpuSpecs);

            dtos.add(gameReqCompDto);

        }
        return dtos;
    }


    public List<GpuRecDto> getAllGpuModels() {
        return gpuModelRepository.findAll().stream()
                .map(GpuRecDto::toDto).toList();


    }

    public List<CpuRecDto> getAllProcessors() {
        return processorRepository.findAll().stream()
                .filter(p -> p.getBenchmark() != null)
                .sorted(Comparator.comparing(Processor::getBenchmark).reversed())
                .map(CpuRecDto::toDto).toList();
    }
    @Transactional
    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }

    @Transactional
    public void saveGameAndSpecs(GameReqCompDto dto, MultipartFile file) throws IOException {
        if (dto == null) {
            throw new IllegalArgumentException("dto must not be null");
        }

        Game game = new Game();
        game.setTitle(dto.getTitle());

        if (file != null && !file.isEmpty()) {
            game.setImage(file.getBytes());
        }

        List<Long> cpuIds = dto.getCpuSpecs() == null ? List.of() :
                dto.getCpuSpecs().stream().map(CpuRecDto::getProcessorId).toList();
        List<Long> gpuIds = dto.getGpuSpecs() == null ? List.of() :
                dto.getGpuSpecs().stream().map(GpuRecDto::getGpuModelId).toList();

        Map<Long, Processor> procMap = processorRepository.findAllById(cpuIds)
                .stream().collect(Collectors.toMap(Processor::getId, p -> p));
        Map<Long, GpuModel> gpuMap = gpuModelRepository.findAllById(gpuIds)
                .stream().collect(Collectors.toMap(GpuModel::getId, g -> g));

        List<GameCpuRequirements> cpuRequirements = new ArrayList<>();
        if (dto.getCpuSpecs() != null) {
            for (CpuRecDto cpuDto : dto.getCpuSpecs()) {
                Processor proc = procMap.get(cpuDto.getProcessorId());
                if (proc == null) {
                    throw new IllegalArgumentException("Processor with id " + cpuDto.getProcessorId() + " not found");
                }
                GameCpuRequirements gcr = new GameCpuRequirements();
                gcr.setProcessor(proc);
                gcr.setRecGameLevel(cpuDto.getRecGameLevel());
                gcr.setGame(game);
                cpuRequirements.add(gcr);
            }
        }
        game.setGameCpuRequirements(cpuRequirements);

        List<GameGpuRequirements> gpuRequirements = new ArrayList<>();
        if (dto.getGpuSpecs() != null) {
            for (GpuRecDto gpuDto : dto.getGpuSpecs()) {
                GpuModel gpu = gpuMap.get(gpuDto.getGpuModelId());
                if (gpu == null) {
                    throw new IllegalArgumentException("GpuModel with id " + gpuDto.getGpuModelId() + " not found");
                }
                GameGpuRequirements ggr = new GameGpuRequirements();
                ggr.setGpuModel(gpu);
                ggr.setRecGameLevel(gpuDto.getRecGameLevel());
                ggr.setGame(game);
                gpuRequirements.add(ggr);
            }
        }
        game.setGameGpuRequirements(gpuRequirements);

        gameRepository.save(game);
    }


    @Transactional
    public void updateGameReqInfoBulk(GameReqCompDto dto, MultipartFile file) throws IOException {
        Game game = gameRepository.findById(dto.getId()).orElseThrow();

        if (file != null && !file.isEmpty()) game.setImage(file.getBytes());
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) game.setTitle(dto.getTitle());

        gameCpuRequirementsRepository.deleteByGameId(game.getId());
        gameGpuRequirementsRepository.deleteByGameId(game.getId());

        List<GameCpuRequirements> toSaveCpu = new ArrayList<>();
        for (CpuRecDto c : dto.getCpuSpecs()) {
            Processor proc = processorRepository.findById(c.getProcessorId()).orElseThrow();
            GameCpuRequirements gcr = new GameCpuRequirements();
            gcr.setGame(game);
            gcr.setProcessor(proc);
            gcr.setRecGameLevel(c.getRecGameLevel());
            toSaveCpu.add(gcr);
        }
        gameCpuRequirementsRepository.saveAll(toSaveCpu);

        List<GameGpuRequirements> toSaveGpu = new ArrayList<>();

        for (GpuRecDto g : dto.getGpuSpecs()) {
            GpuModel gpu = gpuModelRepository.findById(g.getGpuModelId()).orElseThrow();
            GameGpuRequirements ggr = new GameGpuRequirements();
            ggr.setGame(game);
            ggr.setGpuModel(gpu);
            ggr.setRecGameLevel(g.getRecGameLevel());
            ggr.setGame(game);
            toSaveGpu.add(ggr);
        }
        gameGpuRequirementsRepository.saveAll(toSaveGpu);
        gameRepository.save(game);

    }


    public byte[] getImageBytes(Long id) {
        Optional<Game> byId = gameRepository.findById(id);
        return byId.map(Game::getImage).orElse(null);
    }
}
