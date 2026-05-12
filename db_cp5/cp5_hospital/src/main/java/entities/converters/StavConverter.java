package entities.converters;

import entities.enums.StavEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StavConverter implements AttributeConverter<StavEnum, String> {

    @Override
    public String convertToDatabaseColumn(StavEnum attribute) {
        if (attribute == null) return null;
        return attribute.getValue();
    }

    @Override
    public StavEnum convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (StavEnum e : StavEnum.values()) {
            if (e.getValue().equals(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown value: " + dbData);
    }
}
