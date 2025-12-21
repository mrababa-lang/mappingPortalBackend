package com.slashdata.vehicleportal.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AiProviderConverter implements AttributeConverter<AiProvider, String> {

    @Override
    public String convertToDatabaseColumn(AiProvider attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public AiProvider convertToEntityAttribute(String dbData) {
        return AiProvider.fromValue(dbData);
    }
}
