package org.example.backend_pcbuild.LoginAndRegister.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CredentialsDto {
    private String login;
    private char[] password;
}
