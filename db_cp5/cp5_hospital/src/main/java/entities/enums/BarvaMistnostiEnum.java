package entities.enums;

public enum BarvaMistnostiEnum {
    FIALOVA("fialova"),
    ZELENA("zelena"),
    MODRA("modra"),
    BILA("bila");

    private final String value;

    BarvaMistnostiEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

