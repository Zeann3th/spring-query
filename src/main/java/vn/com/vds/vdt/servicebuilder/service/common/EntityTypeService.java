package vn.com.vds.vdt.servicebuilder.service.common;

import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.CreateEntityTypeRequest;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;

public interface EntityTypeService {
    EntityType createOrUpdate(CreateEntityTypeRequest request);
}
