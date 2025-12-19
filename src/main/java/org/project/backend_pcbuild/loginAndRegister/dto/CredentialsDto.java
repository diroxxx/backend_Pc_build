package org.project.backend_pcbuild.loginAndRegister.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CredentialsDto {
    @NotNull(message = "Email is required")
    @Size(min = 3, max = 20, message = "Email must be between 4 and 40 characters")
    private String login;
    @NotNull(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 40 characters")
    private char[] password;
}
