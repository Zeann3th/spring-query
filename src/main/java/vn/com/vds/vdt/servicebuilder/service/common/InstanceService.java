package vn.com.vds.vdt.servicebuilder.service.common;

import vn.com.vds.vdt.servicebuilder.entity.Instance;

import java.util.Map;

public interface InstanceService {
    Instance createInstance(String entityTypeName, Map<String, Object> attributes);

    Instance updateInstance(Long entityId, Map<String, Object> attributes);
}
