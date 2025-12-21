package com.slashdata.vehicleportal.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum AiProvider {
    GEMINI("gemini"),
    OPENAI("openai");

    private final String value;

    AiProvider(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AiProvider fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (AiProvider provider : values()) {
            if (provider.value.equals(normalized)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unsupported AI provider: " + value);
    }
}
