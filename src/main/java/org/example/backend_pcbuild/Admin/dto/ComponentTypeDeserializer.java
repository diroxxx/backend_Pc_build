package org.example.backend_pcbuild.Admin.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.example.backend_pcbuild.models.ComponentType;

import java.io.IOException;

public class ComponentTypeDeserializer extends JsonDeserializer<ComponentType> {
    @Override
    public ComponentType deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        String value = p.getText();
        try {
            return ComponentType.valueOf(value);
        } catch (IllegalArgumentException e) {
            // Fallback dla starych wartości
            return ComponentType.valueOf(value.toUpperCase().replace(" ", "_"));
        }
    }
}
