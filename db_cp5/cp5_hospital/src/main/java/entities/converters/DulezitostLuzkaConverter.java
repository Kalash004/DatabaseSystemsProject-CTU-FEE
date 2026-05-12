package entities.converters;
import entities.enums.DulezitostLuzkaEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DulezitostLuzkaConverter implements AttributeConverter<DulezitostLuzkaEnum, String> {

    @Override
    public String convertToDatabaseColumn(DulezitostLuzkaEnum attribute) {
        if (attribute == null) return null;
        return attribute.getValue();
    }

    @Override
    public DulezitostLuzkaEnum convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (DulezitostLuzkaEnum e : DulezitostLuzkaEnum.values()) {
            if (e.getValue().equals(dbData)) return e;
        }
        throw new IllegalArgumentException("Unknown value: " + dbData);
    }
}