package org.project.backend_pcbuild.Game.service;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.project.backend_pcbuild.Game.ytDto.GameFpsConfigDto;
import org.project.backend_pcbuild.Game.ytDto.VideoRecommendationDto;
import org.project.backend_pcbuild.Game.ytDto.YoutubeApiResponseDto;
import org.project.backend_pcbuild.Game.ytDto.YtVideoInfoDto;
import org.project.backend_pcbuild.configuration.jwtConfig.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

        if (gameFpsConfigDto.getGameTitle() == null || gameFpsConfigDto.getGameTitle().isEmpty()) {
            throw new AppException("Tytuł gry jest wymagany", HttpStatus.BAD_REQUEST);
        }
        if (gameFpsConfigDto.getCpu() == null || gameFpsConfigDto.getCpu().isEmpty()) {
            throw new AppException("CPU jest wymagany", HttpStatus.BAD_REQUEST);
        }
        if (gameFpsConfigDto.getGpu() == null || gameFpsConfigDto.getGpu().isEmpty()) {
            throw new AppException("GPU jest wymagany", HttpStatus.BAD_REQUEST);
        }

        String query = "benchmark " + gameFpsConfigDto.getGameTitle() + " " 
                    + gameFpsConfigDto.getCpu() + " " 
                    + gameFpsConfigDto.getGpu();

        YoutubeApiResponseDto response = fetchVideos(query);

        if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
            throw new AppException("Nie znaleziono żadnych filmów...", HttpStatus.NOT_FOUND);
        }

        List<String> requiredKeywords = new ArrayList<>();
        requiredKeywords.add(gameFpsConfigDto.getGameTitle());
        requiredKeywords.add(gameFpsConfigDto.getCpu());
        requiredKeywords.add(gameFpsConfigDto.getGpu());

        List<String> optionalKeywords = new ArrayList<>();
        if (gameFpsConfigDto.getResolution() != null) optionalKeywords.add(gameFpsConfigDto.getResolution());
        if (gameFpsConfigDto.getGraphicsPreset() != null) optionalKeywords.add(gameFpsConfigDto.getGraphicsPreset());
        if (gameFpsConfigDto.getTechnology() != null) optionalKeywords.add(gameFpsConfigDto.getTechnology());
        optionalKeywords.add("benchmark");

        List<YoutubeApiResponseDto.Item> items = response.getItems();
        int maxScore = -1;
        YoutubeApiResponseDto.Item bestVideo = null;
        
        for (YoutubeApiResponseDto.Item i : items) {
            String videoId = i.getId() != null ? i.getId().getVideoId() : null;
            if (videoId == null) continue;
            
            YtVideoInfoDto fetch = fetchVideoInfo(videoId);
            if (fetch == null || fetch.getItems() == null || fetch.getItems().isEmpty()) continue;
            
            YtVideoInfoDto.Item videoInfo = fetch.getItems().get(0);
            String title = videoInfo.getSnippet().getTitle();
            String description = videoInfo.getSnippet().getDescription();
            String fullText = title + " " + description;

            int requiredMatches = fuzzyKeywordMatchFlexible(fullText, requiredKeywords, 3);
            
            System.out.println(title + " | Required: " + requiredMatches + "/" + requiredKeywords.size());
            
            if (requiredMatches < 2) {
                continue;
            }

            int optionalMatches = fuzzyKeywordMatch(fullText, optionalKeywords, 2);
            int bonus = (requiredMatches == 3) ? 50 : 0;
            int totalScore = requiredMatches * 10 + optionalMatches + bonus;
            
            System.out.println("  -> Optional: " + optionalMatches + " | Total Score: " + totalScore);
            
            if (totalScore > maxScore) {
                maxScore = totalScore;
                bestVideo = i;
            }
        }

        if (bestVideo == null) {
            throw new AppException("Nie znaleziono filmu spełniającego wymagania (minimum: gra + CPU lub GPU)", HttpStatus.NOT_FOUND);
        }

        VideoRecommendationDto videoRecommendationDto = new VideoRecommendationDto();
        videoRecommendationDto.setId(bestVideo.getId().getVideoId());
        videoRecommendationDto.setTitle(bestVideo.getSnippet().getTitle());
        videoRecommendationDto.setThumbnailUrl(bestVideo.getSnippet().getThumbnails().getMedium().getUrl());
        String videoId = bestVideo.getId().getVideoId();
        String url = "https://www.youtube.com/watch?v=" + videoId;
        videoRecommendationDto.setUrl(url);

        return videoRecommendationDto;
    }

    public static int fuzzyKeywordMatchFlexible(String description, List<String> keywords, int threshold) {
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

            String normalizedKey = lowerKey.replaceAll("[\\s-_]", "");
            String normalizedDesc = lowerDesc.replaceAll("[\\s-_]", "");
            if (normalizedDesc.contains(normalizedKey)) {
                matches++;
                continue;
            }

            String[] keyParts = lowerKey.split("[\\s-_]+");
            boolean partialMatch = false;
            for (String part : keyParts) {
                if (part.length() >= 4 && lowerDesc.contains(part)) {
                    partialMatch = true;
                    break;
                }
            }
            if (partialMatch) {
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

    private YoutubeApiResponseDto fetchVideos(String query){

        URI url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/search")
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("q", query)
                .queryParam("order", "relevance")
                .queryParam("maxResults", "20")
                .queryParam("key", apiKey)
                .build()
                .toUri();

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
