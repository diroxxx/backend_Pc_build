package org.project.backend_pcbuild.configuration.jwtConfig;

import org.project.backend_pcbuild.loginAndRegister.dto.ErrorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class RestExceptionHandler {

    public ResponseEntity<ErrorDto> handleException(AppException ex) {

        return ResponseEntity.status(ex.getCode())
                .body(ErrorDto.builder().message(ex.getMessage()).build());
    }
}
