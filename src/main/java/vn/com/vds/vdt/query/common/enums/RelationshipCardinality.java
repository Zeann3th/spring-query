package vn.com.vds.vdt.query.common.enums;

import lombok.Getter;

@Getter
@SuppressWarnings("all")
public enum RelationshipCardinality {
    ONE_TO_ONE("ONE_TO_ONE"),
    ONE_TO_MANY("ONE_TO_MANY"),
    MANY_TO_ONE("MANY_TO_ONE"),
    MANY_TO_MANY("MANY_TO_MANY"),
    UNKNOWN("UNKNOWN");

    private final String value;

    RelationshipCardinality(String value) {
        this.value = value;
    }

    public static RelationshipCardinality reverse(RelationshipCardinality cardinality) {
        return switch (cardinality) {
            case ONE_TO_MANY -> MANY_TO_ONE;
            case MANY_TO_ONE -> ONE_TO_MANY;
            case ONE_TO_ONE -> ONE_TO_ONE;
            case MANY_TO_MANY -> MANY_TO_MANY;
            default -> UNKNOWN;
        };
    }
}
