package org.project.backend_pcbuild.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private CategoryDTO category;
//    private PostImageDTO postImage;
}
