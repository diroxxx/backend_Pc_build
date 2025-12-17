package org.example.backend_pcbuild.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_pcbuild.Admin.service.OfferAdminService;
import org.example.backend_pcbuild.Component.service.ComponentService;
import org.example.backend_pcbuild.Offer.dto.OfferComponentMapper;
import org.example.backend_pcbuild.configuration.JwtConfig.AppException;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.OfferRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;
    private final ComponentService componentService;
    private final OfferRepository offerRepository;
    private final OfferAdminService offerService;


    @GetMapping("/{id}/image")
    public ResponseEntity<?> getGameImage(@PathVariable Long id) {
        byte[] bytes = gameService.getImageBytes(id);
        if (bytes == null || bytes.length == 0) return ResponseEntity.notFound().build();
        MediaType mediaType = MediaType.IMAGE_PNG;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .body(bytes);
    }

    @GetMapping()
    public ResponseEntity<List<GameDto>> getAllGames() {
        List<GameDto> games = gameService.getAllGames();

        return ResponseEntity.ok(games);
    }

    @GetMapping("/cpu-gpu")
    public ResponseEntity<?> getCpuGpuInfo(@RequestParam("gameTitle") String gameTitle,
                                           @RequestParam(value = "budget", required = false) double budget) {
        if (gameTitle == null || gameTitle.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Game game = gameService.findByTitle(gameTitle);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }

        CpuGpuRecGameDto recGameDto = new CpuGpuRecGameDto();

        Optional<GameCpuRequirements> minCpuReq = game.getGameCpuRequirements().stream()
                .filter(r -> r.getRecGameLevel() == RecGameLevel.MIN)
                .findFirst();
        Optional<GameCpuRequirements> recCpuReq = game.getGameCpuRequirements().stream()
                .filter(r -> r.getRecGameLevel() == RecGameLevel.REC)
                .findFirst();

        Optional<GameGpuRequirements> minGpuReq = game.getGameGpuRequirements().stream()
                .filter(r -> r.getRecGameLevel() == RecGameLevel.MIN)
                .findFirst();
        Optional<GameGpuRequirements> recGpuReq = game.getGameGpuRequirements().stream()
                .filter(r -> r.getRecGameLevel() == RecGameLevel.REC)
                .findFirst();

        minCpuReq
                .map(GameCpuRequirements::getProcessor).flatMap(proc -> offerService.findBestForCpu(offerRepository, proc.getComponent(), budget)
                        .map(offer -> OfferComponentMapper.toDto(proc, offer))).ifPresent(dto -> recGameDto.getMinRec().add(dto));


        minGpuReq
                .map(GameGpuRequirements::getGpuModel)
                .flatMap(gpu -> offerService.findBestForGpuModel(offerRepository, gpu, budget)
                        .map(offer -> OfferComponentMapper.toDto(offer.getComponent().getGraphicsCard(), offer))).ifPresent(dto -> recGameDto.getMinRec().add(dto));

        recCpuReq.map(GameCpuRequirements::getProcessor).flatMap(proc -> offerService.findBestForCpu(offerRepository, proc.getComponent(), budget)
                        .map(offer -> OfferComponentMapper.toDto(proc, offer))).ifPresent(dto -> recGameDto.getMaxRec().add(dto));


        recGpuReq
                .map(GameGpuRequirements::getGpuModel)
                .flatMap(gpu -> offerService.findBestForGpuModel(offerRepository, gpu, budget)
                        .map(offer -> OfferComponentMapper.toDto(offer.getComponent().getGraphicsCard(), offer))).ifPresent(dto -> recGameDto.getMaxRec().add(dto));


        return ResponseEntity.ok(recGameDto);
    }

    @GetMapping("/specs")
    public ResponseEntity<List<GameReqCompDto>> getAllSpecsOfGames() {
        return ResponseEntity.ok(gameService.getAllSpecsOfGames());
    }

    @GetMapping("/cpus")
    public ResponseEntity<List<CpuRecDto>> getAllProcessors() {
        return ResponseEntity.ok(gameService.getAllProcessors());
    }

    @GetMapping("/gpus")
    public ResponseEntity<List<GpuRecDto>> getAllGpuModels() {
        return ResponseEntity.ok(gameService.getAllGpuModels());
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteGameById(@Param("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        gameService.deleteGame(id);
        return ResponseEntity.ok(Map.of("message", "Gra została usunięta"));
    }

//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> insertNewGameReqInfo(@RequestPart("file") MultipartFile file, @RequestPart("dto") GameReqCompDto dto) {

        if (file.isEmpty() || file.getOriginalFilename() == null || dto == null || dto.getTitle() == null || dto.getCpuSpecs().isEmpty() || dto.getGpuSpecs().isEmpty()) {
            throw new AppException("Brak danych do zapisu", HttpStatus.BAD_REQUEST);
        }
        try {
            gameService.saveGameAndSpecs(dto,file);

        }catch (Exception e){
            throw new AppException("Podczas zapisywania gry wystapił bład", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok("");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateGameReqInfo(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart(value = "dto",required = false) GameReqCompDto dto) {

        if (file == null && dto == null) {
            throw new AppException("Zadne zmiany nie zostały wprowadzone", HttpStatus.BAD_REQUEST);
        }

        if (gameService.findByTitle(dto.getTitle()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Tytuł już istnieje w bazie"));
        }

        try{
            gameService.updateGameReqInfoBulk(dto, file);

        }catch (Exception e){
            throw new AppException("Edycja gry nie powiodła się", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(Map.of("message", "Gra została zaktualizowana"));
    }
}
