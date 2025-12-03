package org.example.backend_pcbuild.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend_pcbuild.Community.DTO.CategoryDTO;

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
}
