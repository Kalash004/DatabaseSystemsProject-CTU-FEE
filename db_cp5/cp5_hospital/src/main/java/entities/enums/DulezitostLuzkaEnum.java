package entities.enums;


public enum DulezitostLuzkaEnum {
    BEZNA("běžná"),
    INTENZIVNI("intenzivní"),
    JEDNOTKA_INTENZIVNI_PECE("jednotka_intenzivní_péče");

    private final String value;

    DulezitostLuzkaEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}