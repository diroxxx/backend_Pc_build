package org.project.backend_pcbuild.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PostPreviewDTO {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;

    private String categoryName;
    private Long thumbnailImageId;
}