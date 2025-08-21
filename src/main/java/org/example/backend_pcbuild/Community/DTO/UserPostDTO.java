package org.example.backend_pcbuild.Community.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserPostDTO {
    private Long id;
    private String username;

}
