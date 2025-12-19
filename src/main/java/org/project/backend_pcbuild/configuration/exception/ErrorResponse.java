package org.project.backend_pcbuild.configuration.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        int status
//        LocalDateTime timestamp
) {}