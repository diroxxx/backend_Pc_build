package org.example.backend_pcbuild.Community.DTO;


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

        // POPRAWNY URL
        this.imageUrl = "/community/image/" + id;
    }

}