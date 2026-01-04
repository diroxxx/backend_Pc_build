package org.project.backend_pcbuild.pcComponents.dto;

import org.project.backend_pcbuild.pcComponents.model.ComponentType;

public record ComponentsAmountPc(String componentType, String model, Long amount) {
}
