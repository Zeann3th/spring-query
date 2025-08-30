package vn.com.vds.vdt.servicebuilder.service.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.vds.vdt.servicebuilder.entity.Instance;

import java.util.Map;

public interface InstanceService {
    Long createInstance(String entityTypeName, Map<String, Object> attributes);

    void updateInstance(Long entityId, Map<String, Object> attributes);

    void deleteInstance(Long entityId);

    Page<Instance> getInstances(String entityTypeName, Pageable pageable);

    Instance getInstanceById(Long entityId);
}
