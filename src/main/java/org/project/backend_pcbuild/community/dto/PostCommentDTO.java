package org.project.backend_pcbuild.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class PostCommentDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private UserPostDTO user;
    private PostDTO post;
    private String username;
}
