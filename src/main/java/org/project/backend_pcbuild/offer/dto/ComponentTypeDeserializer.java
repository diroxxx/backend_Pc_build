package org.project.backend_pcbuild.offer.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;

import java.io.IOException;

public class ComponentTypeDeserializer extends JsonDeserializer<ComponentType> {
    @Override
    public ComponentType deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        String value = p.getText();
        try {
            return ComponentType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return ComponentType.valueOf(value.toUpperCase().replace(" ", "_"));
        }
    }
}
