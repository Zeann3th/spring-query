package vn.com.vds.vdt.servicebuilder.controller.dto.instance;

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
public class InstanceUpdateRequest {
    private Map<String, Object> attributes;
}