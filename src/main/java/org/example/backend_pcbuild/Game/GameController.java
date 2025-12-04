package org.example.backend_pcbuild.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_pcbuild.Component.service.ComponentService;
import org.example.backend_pcbuild.Offer.dto.OfferComponentMapper;
import org.example.backend_pcbuild.YoutubeGameRecomendation.dto.GameFpsConfigDto;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<?> getCpuGpuInfo(@Param("gameTitle") String gameTitle, @Param("budget") Double budget) {
        if (gameTitle == null || budget == null) {
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

        if (minCpuReq.isPresent() && minCpuReq.get().getProcessor() != null) {
            Processor proc = minCpuReq.get().getProcessor();
            Component component = proc.getComponent();

            if (component != null) {
                Optional<Offer> cheapestCpu = offerRepository.findCheapestNative(component.getId());
                cheapestCpu.ifPresent(o -> recGameDto.getMinRec().add(OfferRecDto.toDto(o)));
            }
        }

        if (minGpuReq.isPresent() && minGpuReq.get().getGpuModel() != null) {
            GpuModel gm = minGpuReq.get().getGpuModel();
            List<Offer> cheapestGpu = offerRepository.findTopByGpuModelOrderByPriceAsc(gm);
            System.out.println(cheapestGpu.size());
            if (!cheapestGpu.isEmpty()) recGameDto.getMinRec().add(OfferRecDto.toDto(cheapestGpu.get(0)));
        }

        if (recCpuReq.isPresent() && recCpuReq.get().getProcessor() != null) {
            Processor proc = recCpuReq.get().getProcessor();
            Component component = proc.getComponent();
            if (component != null) {
                Optional<Offer> recCpuOffer = offerRepository.findCheapestNative(component.getId());
                recCpuOffer.ifPresent(o -> recGameDto.getMaxRec().add(OfferRecDto.toDto(o)));
            }
        }

        if (recGpuReq.isPresent() && recGpuReq.get().getGpuModel() != null) {
            GpuModel gm = recGpuReq.get().getGpuModel();
            List<Offer> recGpuOffer = offerRepository.findTopByGpuModelOrderByPriceAsc(gm);
            System.out.println(recGpuOffer.size());
            if (!recGpuOffer.isEmpty()) recGameDto.getMaxRec().add(OfferRecDto.toDto(recGpuOffer.get(0)));

        }

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

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
//        if(file == null ){
//            throw new AppException("Brak danych do zapisu", HttpStatus.BAD_REQUEST);
//        }
//        System.out.println(file.getOriginalFilename());
        try{
//            gameService.updateGameReqInfo(dto, file);
            gameService.updateGameReqInfoBulk(dto, file);

        }catch (Exception e){
            throw new AppException("Edycja gry nie powiodła się", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(Map.of("message", "Gra została zaktualizowana"));
    }




}
