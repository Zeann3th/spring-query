package vn.com.vds.vdt.servicebuilder.controller.dto.instance;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InstanceCreateRequest {
    private String entityType; // entity type name
    private Map<String, Object> attributes;
}
