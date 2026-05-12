package entities.enums;

public enum OddeleniEnum {
    CHIRURGIE("chirurgie"),
    INTERNA("interna"),
    NEUROLOGIE("neurologie"),
    KARDIOLOGIE("kardiologie"),
    PEDIATRIE("pediatrie"),
    PSYCHIATRIE("psychiatrie"),
    ORTOPEDIE("ortopedie"),
    ONKOLOGIE("onkologie");

    private final String value;

    OddeleniEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
