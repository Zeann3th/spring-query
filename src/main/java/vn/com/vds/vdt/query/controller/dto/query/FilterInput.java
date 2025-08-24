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
public class FilterInput {
    private String entityName;
    private Map<String, Object> attributeFilters;
    private Integer limit;
    private Integer offset;
}
