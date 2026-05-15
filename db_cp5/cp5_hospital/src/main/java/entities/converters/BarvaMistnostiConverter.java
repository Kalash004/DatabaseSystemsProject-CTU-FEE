package entities.converters;

import entities.enums.BarvaMistnostiEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public class BarvaMistnostiConverter implements AttributeConverter<BarvaMistnostiEnum, String> {

    @Override
    public String convertToDatabaseColumn(BarvaMistnostiEnum attribute) {
        if (attribute == null) return null;
        return attribute.getValue();
    }

    @Override
    public BarvaMistnostiEnum convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (BarvaMistnostiEnum e : BarvaMistnostiEnum.values()) {
            if (e.getValue().equals(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown value: " + dbData);
    }

}
