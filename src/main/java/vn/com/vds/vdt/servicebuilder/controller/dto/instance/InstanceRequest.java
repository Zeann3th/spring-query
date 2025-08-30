package vn.com.vds.vdt.servicebuilder.controller.dto.instance;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InstanceRequest {
    private Map<String, Object> attributes;
}
