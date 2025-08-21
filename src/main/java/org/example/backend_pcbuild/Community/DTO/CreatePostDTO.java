package org.example.backend_pcbuild.Community.DTO;


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
    private Long userId;
    private Long categoryId;
}
