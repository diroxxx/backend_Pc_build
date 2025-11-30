package org.example.backend_pcbuild.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.example.backend_pcbuild.Community.DTO.CategoryDTO;

@Data
@Builder
@AllArgsConstructor
public class UserPostsDTO {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private CategoryDTO category;
}