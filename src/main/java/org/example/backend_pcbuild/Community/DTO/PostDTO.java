package org.example.backend_pcbuild.Community.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.example.backend_pcbuild.Community.DTO.CategoryDTO;

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
