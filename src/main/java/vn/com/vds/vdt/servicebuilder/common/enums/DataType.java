package vn.com.vds.vdt.servicebuilder.common.enums;

import lombok.Getter;

@Getter
@SuppressWarnings("all")
public enum DataType {
    STRING("STRING"),
    INTEGER("INTEGER"),
    LONG("LONG"),
    DOUBLE("DOUBLE"),
    BOOLEAN("BOOLEAN"),
    DATE("DATE"),
    DATETIME("DATETIME"),
    UUID("UUID");

    private final String value;

    DataType(String value) {
        this.value = value;
    }
}
