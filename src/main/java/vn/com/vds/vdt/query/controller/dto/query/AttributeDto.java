package vn.com.vds.vdt.query.controller.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDto {
    private Long attributeId;
    private String attributeName;
    private String attributeType;
}
