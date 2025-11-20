package org.example.backend_pcbuild.YoutubeGameRecomendation;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.configuration.JwtConfig.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
public class GameRecommendationService {

    @Value("${youtube.api.key}")
    private String apiKey;

    public VideoRecommendationDto getRecommendedVideo(GameFpsConfigDto gameFpsConfigDto){


        if(apiKey == null || apiKey.isEmpty()){
            throw new AppException("Brak klucza API do Youtube", HttpStatus.BAD_REQUEST);
        }
//        String query = String.format("%s %s %s %s",
//                gameFpsConfigDto.getGameTitle(),
//                gameFpsConfigDto.getResolution(),
//                gameFpsConfigDto.getGraphicsPreset(),
//                gameFpsConfigDto.getTechnology()
//                );
        String query = "benchmark rtx 4060 ti ";
        if (gameFpsConfigDto.getGameTitle() != null) query += gameFpsConfigDto.getGameTitle() + " ";
        if (gameFpsConfigDto.getCpu() != null) query += gameFpsConfigDto.getCpu() + " ";
//        if (gameFpsConfigDto.getGpu() != null) query += gameFpsConfigDto.getGpu() + " ";
//        if (gameFpsConfigDto.getResolution() != null) query += gameFpsConfigDto.getResolution() + " ";
//        if (gameFpsConfigDto.getGraphicsPreset() != null) query += gameFpsConfigDto.getGraphicsPreset() + " ";


//        String[] queries = {
//                gameFpsConfigDto.getGameTitle() + " " + gameFpsConfigDto.getGpu(),
//                gameFpsConfigDto.getGameTitle() + " " + gameFpsConfigDto.getGpu() + " benchmark",
//                gameFpsConfigDto.getGameTitle() + " " + gameFpsConfigDto.getCpu() + " " + gameFpsConfigDto.getGpu(),
//        };

        YoutubeApiResponseDto response = null;
        response = fetchVideos(query);

//        for (String q : queries) {
//            response = fetchVideos(q);
//            if (response.getItems() != null && !response.getItems().isEmpty()) {
//                break;
//            }
//        }
        if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
            throw new AppException("Nie znaleziono żadnych filmów...", HttpStatus.NOT_FOUND);
        }
        YoutubeApiResponseDto.Item item = response.getItems().get(0);

        VideoRecommendationDto videoRecommendationDto = new VideoRecommendationDto();
        videoRecommendationDto.setId(item.getId().getVideoId());
        videoRecommendationDto.setTitle(item.getSnippet().getTitle());
        videoRecommendationDto.setThumbnailUrl(item.getSnippet().getThumbnails().getMedium().getUrl());
        String videoId = item.getId().getVideoId();
        String url = "https://www.youtube.com/watch?v=" + videoId;
        videoRecommendationDto.setUrl(url + videoId);

        return videoRecommendationDto;
    }

    private YoutubeApiResponseDto fetchVideos(String query){

        URI url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/search")
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("q", query)
                .queryParam("order", "relevance")
                .queryParam("maxResults", "10")
                .queryParam("key", apiKey)
                .build()
                .toUri();
        System.out.println("Built URL: " + url);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, YoutubeApiResponseDto.class);
    }



}
