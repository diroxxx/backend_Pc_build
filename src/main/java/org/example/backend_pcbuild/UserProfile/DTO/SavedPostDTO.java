package org.example.backend_pcbuild.UserProfile.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.CategoryDTO;
import org.example.backend_pcbuild.models.User;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavedPostDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private String title;
    private String content;
    private CategoryDTO category;
    private String authorName;
    private Long imageId;

    private LocalDateTime createdAt;
}
