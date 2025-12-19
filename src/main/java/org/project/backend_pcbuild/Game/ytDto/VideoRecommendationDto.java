package org.project.backend_pcbuild.Game.ytDto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VideoRecommendationDto {
    private String id;
    private String title;
    private String url;
    private String thumbnailUrl;
    private Double timestamp;
}
