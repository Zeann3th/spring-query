package vn.com.vds.vdt.query.controller.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityInstanceDto {
    private Long instanceId;
    private String entityName;
    private Map<String, Object> values;
    private String createdAt;
    private String updatedAt;
}