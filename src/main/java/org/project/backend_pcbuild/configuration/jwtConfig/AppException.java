package org.project.backend_pcbuild.configuration.jwtConfig;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException  {
    private final HttpStatus code;
    public AppException(String message, HttpStatus code) {
        super(message);
        this.code = code;
    }

    public HttpStatus getStatus() {
        return code;
    }
}
