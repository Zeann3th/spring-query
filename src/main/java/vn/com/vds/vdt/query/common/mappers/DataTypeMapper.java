package vn.com.vds.vdt.query.common.mappers;

import vn.com.vds.vdt.query.common.enums.DataType;

public class DataTypeMapper {

    public static String map(String sqlType) {
        String normalized = sqlType.toUpperCase();

        if (normalized.contains("CHAR") || normalized.contains("TEXT") || normalized.contains("CLOB")) {
            return DataType.STRING.getValue();
        } else if (normalized.contains("INT")) {
            return DataType.INTEGER.getValue();
        } else if (normalized.contains("BIGINT") || normalized.contains("LONG")) {
            return DataType.LONG.getValue();
        } else if (normalized.contains("DOUBLE") || normalized.contains("FLOAT")
                || normalized.contains("DECIMAL") || normalized.contains("NUMERIC")) {
            return DataType.DOUBLE.getValue();
        } else if (normalized.contains("BOOL")) {
            return DataType.BOOLEAN.getValue();
        } else if (normalized.contains("DATE") || normalized.contains("TIME")) {
            return normalized.contains("TIME") ? DataType.DATETIME.getValue() : DataType.DATE.getValue();
        } else if (normalized.contains("UUID")) {
            return DataType.UUID.getValue();
        }

        throw new IllegalArgumentException("Unsupported SQL type: " + sqlType);
    }
}