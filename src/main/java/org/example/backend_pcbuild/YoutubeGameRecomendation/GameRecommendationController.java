package org.example.backend_pcbuild.YoutubeGameRecomendation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/yt/recommendations")
@RequiredArgsConstructor
public class GameRecommendationController {

    private final GameRecommendationService gameRecommendationService;

    @PostMapping
    public ResponseEntity<VideoRecommendationDto> getRecommendedVideo(@RequestBody GameFpsConfigDto gameFpsConfigDto) {

        VideoRecommendationDto videoRecommendationDto = gameRecommendationService.getRecommendedVideo(gameFpsConfigDto);

        return ResponseEntity.ok(videoRecommendationDto);
    }





}
