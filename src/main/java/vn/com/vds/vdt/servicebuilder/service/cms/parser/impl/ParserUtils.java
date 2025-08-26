package vn.com.vds.vdt.servicebuilder.service.cms.parser.impl;

import vn.com.vds.vdt.servicebuilder.entity.EntityType;

public class ParserUtils {
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
