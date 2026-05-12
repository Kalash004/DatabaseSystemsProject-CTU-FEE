package entities.converters;

import entities.enums.OddeleniEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OddeleniConverter implements AttributeConverter<OddeleniEnum, String> {

    @Override
    public String convertToDatabaseColumn(OddeleniEnum attribute) {
        if (attribute == null) return null;
        return attribute.getValue();
    }

    @Override
    public OddeleniEnum convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (OddeleniEnum e : OddeleniEnum.values()) {
            if (e.getValue().equals(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown value: " + dbData);
    }
}
