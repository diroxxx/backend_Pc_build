package org.example.backend_pcbuild.configuration.JwtConfig;

import ch.qos.logback.core.joran.spi.DefaultClass;
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
