package org.example.backend_pcbuild.YoutubeGameRecomendation.dto;

import lombok.Data;

import java.util.List;

@Data
public class YtVideoInfoDto {
    private List<YtVideoInfoDto.Item> items;

    @Data
    public static class Item {
        private String id;
        private Snippet snippet;
    }

    @Data
    public static class Snippet {
        private String title;
        private String description;
        private YoutubeApiResponseDto.Thumbnails thumbnails;
    }

}
