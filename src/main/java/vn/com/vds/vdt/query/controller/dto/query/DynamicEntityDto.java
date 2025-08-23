package vn.com.vds.vdt.query.controller.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicEntityDto {
    private Long entityId;
    private String entityName;
    private List<AttributeDto> attributes;
    private Map<String, Object> values;
}
