package org.example.backend_pcbuild.LoginAndRegister.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String categoryName;
    private LocalDateTime createdAt;
    private String authorName;
}



