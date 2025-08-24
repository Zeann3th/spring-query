package vn.com.vds.vdt.query.controller.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResult<T> {
    private List<T> items;
    private Integer totalCount;
    private Boolean hasNext;
}