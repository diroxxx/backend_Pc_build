package org.project.backend_pcbuild.community.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostImageDTO {

    private Long id;
    private String filename;
    private String mimeType;
    private String imageUrl;

    public PostImageDTO(Long id, String filename, String mimeType) {
        this.id = id;
        this.filename = filename;
        this.mimeType = mimeType;
        this.imageUrl = "http://localhost:8080/community/image/" + id;
    }

}