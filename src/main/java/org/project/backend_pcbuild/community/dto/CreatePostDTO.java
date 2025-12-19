package org.project.backend_pcbuild.community.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreatePostDTO {
    private String title;
    private String content;
    private String email;
    private Long categoryId;
}
