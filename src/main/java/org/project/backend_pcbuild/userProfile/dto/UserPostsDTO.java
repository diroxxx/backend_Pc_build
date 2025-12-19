package org.project.backend_pcbuild.userProfile.dto;

import lombok.*;
import org.project.backend_pcbuild.community.dto.CategoryDTO;

import java.time.LocalDateTime;


@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
public class UserPostsDTO {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private CategoryDTO category;
    private Long imageId;

    private LocalDateTime createdAt;
}