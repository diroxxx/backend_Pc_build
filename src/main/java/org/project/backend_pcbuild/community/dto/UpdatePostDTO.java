package org.project.backend_pcbuild.community.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdatePostDTO {
    private String content;
}
