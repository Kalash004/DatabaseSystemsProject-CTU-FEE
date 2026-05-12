package entities.enums;

public enum StavEnum {
    AKTIVNI("aktivní"),
    NEAKTIVNI("neaktivní"),
    ARCHIVOVANY("archivovaný");

    private final String value;

    StavEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}