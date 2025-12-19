package org.project.backend_pcbuild.Game.controller;

import lombok.RequiredArgsConstructor;
import org.project.backend_pcbuild.Game.ytDto.GameFpsConfigDto;
import org.project.backend_pcbuild.Game.ytDto.VideoRecommendationDto;
import org.project.backend_pcbuild.Game.service.GameRecommendationService;
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
