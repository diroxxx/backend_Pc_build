package org.project.backend_pcbuild.loginAndRegister.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class ErrorDto {
    private String message;

}
