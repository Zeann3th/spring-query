package vn.com.vds.vdt.servicebuilder.controller.dto.entityType;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEntityTypeRequest {
    private Long entityTypeId;
    private String displayName;
    private Boolean isActive;
    private List<AttributeCreate> attributes;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeCreate {
        private String name;
        private String displayName;
        private String dataType;
        private Boolean isRequired;
        private String defaultValue;
        private Map<String, String> validationRules;
    }
}
