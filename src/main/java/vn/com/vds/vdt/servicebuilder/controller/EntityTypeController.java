package vn.com.vds.vdt.servicebuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import vn.com.vds.vdt.servicebuilder.common.base.ResponseWrapper;
import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.CreateEntityTypeRequest;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;
import vn.com.vds.vdt.servicebuilder.service.common.EntityTypeService;

@RestController
@RequestMapping("/api/v1/entities")
@RequiredArgsConstructor
@ResponseWrapper
@SuppressWarnings("all")
public class EntityTypeController {

    private final EntityTypeService entityTypeService;

    @PostMapping
    public EntityType createOrUpdate(@RequestBody CreateEntityTypeRequest request) {
        return entityTypeService.createOrUpdate(request);
    }
}
