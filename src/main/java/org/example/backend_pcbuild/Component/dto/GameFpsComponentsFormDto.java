package org.example.backend_pcbuild.Component.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class GameFpsComponentsFormDto {

    private Set<String> cpusModels = new HashSet<>();
    private Set<String> gpusModels = new HashSet<>();

}
