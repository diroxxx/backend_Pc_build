package org.example.backend_pcbuild.YoutubeGameRecomendation;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.example.backend_pcbuild.configuration.JwtConfig.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class GameRecommendationService {

    @Value("${youtube.api.key}")
    private String apiKey;

    public VideoRecommendationDto getRecommendedVideo(GameFpsConfigDto gameFpsConfigDto){


        if(apiKey == null || apiKey.isEmpty()){
            throw new AppException("Brak klucza API do Youtube", HttpStatus.BAD_REQUEST);
        }

        String query = "benchmark ";
        if (gameFpsConfigDto.getGameTitle() != null) query += gameFpsConfigDto.getGameTitle() + " ";
        if (gameFpsConfigDto.getCpu() != null) query += gameFpsConfigDto.getCpu() + " ";

        YoutubeApiResponseDto response = null;
        response = fetchVideos(query);

        if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
            throw new AppException("Nie znaleziono żadnych filmów...", HttpStatus.NOT_FOUND);
        }

        List<String> keywords = new ArrayList<>();
        if (gameFpsConfigDto.getGameTitle() != null) keywords.add(gameFpsConfigDto.getGameTitle());
        if (gameFpsConfigDto.getCpu() != null) keywords.add(gameFpsConfigDto.getCpu());
        if (gameFpsConfigDto.getGpu() != null) keywords.add(gameFpsConfigDto.getGpu());
        if (gameFpsConfigDto.getResolution() != null) keywords.add(gameFpsConfigDto.getResolution());
        if (gameFpsConfigDto.getGraphicsPreset() != null) keywords.add(gameFpsConfigDto.getGraphicsPreset());
        if (gameFpsConfigDto.getTechnology() != null) keywords.add(gameFpsConfigDto.getTechnology());
        keywords.add("benchmark");

        List<YoutubeApiResponseDto.Item> items = response.getItems();
        int maxScore = 0;
        YoutubeApiResponseDto.Item bestVideo = new YoutubeApiResponseDto.Item();
        for (YoutubeApiResponseDto.Item i : items) {
            String videoId = i.getId() != null ? i.getId().getVideoId() : null;
            if (videoId == null) continue;
            YtVideoInfoDto fetch = fetchVideoInfo(videoId);
            if (fetch == null || fetch.getItems() == null || fetch.getItems().isEmpty()) continue;
            YtVideoInfoDto.Item videoInfo = fetch.getItems().get(0);
            String description = videoInfo.getSnippet().getDescription();

//            System.out.println(description);
            int scoreSimilarity = fuzzyKeywordMatch(description, keywords, 2);
//            maxScore= Math.max(maxScore, scoreSimilarity);
            System.out.println(videoInfo.getSnippet().getTitle() + " " + scoreSimilarity);
            if (scoreSimilarity > maxScore) {
                maxScore = scoreSimilarity;
                bestVideo = i;
            }
        }
        VideoRecommendationDto videoRecommendationDto = new VideoRecommendationDto();
        videoRecommendationDto.setId(bestVideo.getId().getVideoId());
        videoRecommendationDto.setTitle(bestVideo.getSnippet().getTitle());
        videoRecommendationDto.setThumbnailUrl(bestVideo.getSnippet().getThumbnails().getMedium().getUrl());
        String videoId = bestVideo.getId().getVideoId();
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
//        System.out.println("Built URL: " + url);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, YoutubeApiResponseDto.class);
    }

    private YtVideoInfoDto fetchVideoInfo(String id){

        URI url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/videos")
                .queryParam("part", "snippet")
                .queryParam("id", id)
                .queryParam("key", apiKey)
                .build()
                .toUri();
//        System.out.println("Built URL: " + url);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, YtVideoInfoDto.class);
    }

    public static int fuzzyKeywordMatch(String description, List<String> keywords, int threshold) {
        if (description == null || keywords == null) return 0;
        int matches = 0;
        String lowerDesc = description.toLowerCase();
        LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();

        for (String keyword : keywords) {
            if (keyword == null) continue;
            String lowerKey = keyword.toLowerCase();

            if (lowerDesc.contains(lowerKey)) {
                matches++;
                continue;
            }
            String[] words = lowerDesc.split("[\\s,;:.!\\?]+");
            for (String word : words) {
                Integer distance = levenshtein.apply(lowerKey, word);
                if (distance != null && distance <= threshold) {
                    matches++;
                    break;
                }
            }
        }
        return matches;
    }



}
