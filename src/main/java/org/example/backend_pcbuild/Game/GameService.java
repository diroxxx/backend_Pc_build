package org.example.backend_pcbuild.Game;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Component.service.ComponentService;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final ComponentService componentService;
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
//        dto.setFile(game.getImage());

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
        return gpuModelRepository.findAll().stream().map(GpuRecDto::toDto).toList();


    }

    public List<CpuRecDto> getAllProcessors() {
        return processorRepository.findAll().stream().map(CpuRecDto::toDto).toList();
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

    }



//    @Transactional
//    public void updateGameReqInfo(GameReqCompDto dto, MultipartFile file) throws IOException {
//        Game game = gameRepository.findById(dto.getId()).orElseThrow(() -> new NoSuchElementException("Game not found"));
//
//        if (file != null && !file.isEmpty()) {
//            game.setImage(file.getBytes());
//        }
//        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
//            game.setTitle(dto.getTitle());
//        }
//
//        List<CpuRecDto> incomingCpu = dto.getCpuSpecs() == null ? List.of() : dto.getCpuSpecs();
//        List<GpuRecDto> incomingGpu = dto.getGpuSpecs() == null ? List.of() : dto.getGpuSpecs();
//
//        List<Long> cpuIds = incomingCpu.stream().map(CpuRecDto::getProcessorId).filter(Objects::nonNull).toList();
//        List<Long> gpuIds = incomingGpu.stream().map(GpuRecDto::getGpuModelId).filter(Objects::nonNull).toList();
//
//        Map<Long, Processor> procMap = processorRepository.findAllById(cpuIds)
//                .stream().collect(Collectors.toMap(Processor::getId, p -> p));
//        Map<Long, GpuModel> gpuMap = gpuModelRepository.findAllById(gpuIds)
//                .stream().collect(Collectors.toMap(GpuModel::getId, g -> g));
//
//        Map<Long, GameCpuRequirements> existingCpuMap = game.getGameCpuRequirements().stream()
//                .filter(gcr -> gcr.getProcessor() != null && (gcr.getProcessor().getId() != null))
//                .collect(Collectors.toMap(gcr -> gcr.getProcessor().getId(), gcr -> gcr));
//
//        Set<Long> incomingCpuIdSet = new HashSet<>(cpuIds);
//
//        for (CpuRecDto cpuRecDto : incomingCpu) {
//            Long pid = cpuRecDto.getProcessorId();
//            if (pid == null) continue;
//            Processor proc = procMap.get(pid);
//            if (proc == null) {
//                continue;
//            }
//            GameCpuRequirements existing = existingCpuMap.get(pid);
//            if (existing != null) {
//                existing.setRecGameLevel(cpuRecDto.getRecGameLevel());
//                existingCpuMap.remove(pid);
//            } else {
//                GameCpuRequirements newGcr = new GameCpuRequirements();
//                newGcr.setProcessor(proc);
//                newGcr.setRecGameLevel(cpuRecDto.getRecGameLevel());
//                newGcr.setGame(game);
//                game.getGameCpuRequirements().add(newGcr);
//            }
//        }
//
//        if (!existingCpuMap.isEmpty()) {
//            Iterator<GameCpuRequirements> it = game.getGameCpuRequirements().iterator();
//            while (it.hasNext()) {
//                GameCpuRequirements gcr = it.next();
//                Long existingPid = gcr.getProcessor() != null ? gcr.getProcessor().getId() : null;
//                if (existingPid != null && !incomingCpuIdSet.contains(existingPid)) {
//                    it.remove();
//                }
//            }
//        }
//
//        Map<Long, GameGpuRequirements> existingGpuMap = game.getGameGpuRequirements().stream()
//                .filter(ggr -> ggr.getGpuModel() != null && ggr.getGpuModel().getId() != null)
//                .collect(Collectors.toMap(ggr -> ggr.getGpuModel().getId(), ggr -> ggr));
//
//        Set<Long> incomingGpuIdSet = new HashSet<>(gpuIds);
//
//        for (GpuRecDto gpuRecDto : incomingGpu) {
//            Long gid = gpuRecDto.getGpuModelId();
//            if (gid == null) continue;
//            GpuModel gpuModel = gpuMap.get(gid);
//            if (gpuModel == null) {
//                continue;
//            }
//            GameGpuRequirements existing = existingGpuMap.get(gid);
//            if (existing != null) {
//                existing.setRecGameLevel(gpuRecDto.getRecGameLevel());
//                existingGpuMap.remove(gid);
//            } else {
//                GameGpuRequirements newGgr = new GameGpuRequirements();
//                newGgr.setGpuModel(gpuModel);
//                newGgr.setRecGameLevel(gpuRecDto.getRecGameLevel());
//                newGgr.setGame(game);
//                game.getGameGpuRequirements().add(newGgr);
//            }
//        }
//
//        if (!existingGpuMap.isEmpty()) {
//            Iterator<GameGpuRequirements> it = game.getGameGpuRequirements().iterator();
//            while (it.hasNext()) {
//                GameGpuRequirements ggr = it.next();
//                Long existingGid = ggr.getGpuModel() != null ? ggr.getGpuModel().getId() : null;
//                if (existingGid != null && !incomingGpuIdSet.contains(existingGid)) {
//                    it.remove();
//                }
//            }
//        }
//
//        gameRepository.save(game);
//    }

    public byte[] getImageBytes(Long id) {
        Optional<Game> byId = gameRepository.findById(id);
        return byId.map(Game::getImage).orElse(null);
    }
}
