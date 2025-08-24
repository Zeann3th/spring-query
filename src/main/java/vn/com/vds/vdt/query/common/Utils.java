package vn.com.vds.vdt.query.common;

import vn.com.vds.vdt.query.entity.EntityType;
import vn.com.vds.vdt.query.service.parser.impl.MermaidSchemaParser;

public class Utils {
    public static String generateRelationshipName(EntityType fromEntity, EntityType toEntity,
                                            String fkName, boolean isReverse) {
        String baseName = (fkName != null && !fkName.trim().isEmpty()) ?
                fkName :
                fromEntity.getName() + "_to_" + toEntity.getName();

        return isReverse ? "reverse_" + baseName : baseName;
    }

    public static String capitalize(String name) {
        StringBuilder sb = new StringBuilder();
        for (String word : name.split("_")) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
