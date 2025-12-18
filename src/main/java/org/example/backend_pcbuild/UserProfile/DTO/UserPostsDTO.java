package org.example.backend_pcbuild.UserProfile.DTO;

import lombok.*;
import org.example.backend_pcbuild.Community.DTO.CategoryDTO;

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