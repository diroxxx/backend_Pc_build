package org.project.backend_pcbuild.pcComponents.dto;

import org.project.backend_pcbuild.pcComponents.model.ComponentType;

import java.math.BigInteger;

public record ComponentsAmountPc(String componentType, String model, Integer amount) {
}
