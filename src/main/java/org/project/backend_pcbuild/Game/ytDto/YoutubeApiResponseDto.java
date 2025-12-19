package org.project.backend_pcbuild.Game.ytDto;

import lombok.Data;
import java.util.List;

@Data
public class YoutubeApiResponseDto {
    private List<Item> items;

    @Data
    public static class Item {
        private Id id;
        private Snippet snippet;
    }

    @Data
    public static class Id {
        private String videoId;
    }

    @Data
    public static class Snippet {
        private String title;
        private String description;
        private Thumbnails thumbnails;
    }

    @Data
    public static class Thumbnails {
        private Thumbnail high;
        private Thumbnail medium;
        private Thumbnail defaultThumbnail;
    }

    @Data
    public static class Thumbnail {
        private String url;
        private int width;
        private int height;
    }
}