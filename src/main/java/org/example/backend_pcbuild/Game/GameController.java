package org.example.backend_pcbuild.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_pcbuild.Component.service.ComponentService;
import org.example.backend_pcbuild.Offer.dto.OfferComponentMapper;
import org.example.backend_pcbuild.YoutubeGameRecomendation.dto.GameFpsConfigDto;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.OfferRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;
    private final ComponentService componentService;
    private final OfferRepository offerRepository;

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
            OfferRecDto offerRecDto = OfferRecDto.toDto(recGpuOffer.get(0));
            if (!recGpuOffer.isEmpty()) recGameDto.getMaxRec().add(OfferRecDto.toDto(recGpuOffer.get(0)));

        }

        return ResponseEntity.ok(recGameDto);
    }
}
