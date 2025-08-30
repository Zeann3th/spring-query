package vn.com.vds.vdt.servicebuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import vn.com.vds.vdt.servicebuilder.common.base.ResponseWrapper;
import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.CreateEntityTypeRequest;
import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.UpdateEntityTypeRequest;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;
import vn.com.vds.vdt.servicebuilder.service.common.EntityTypeService;

@RestController
@RequestMapping("/api/v1/entity-types")
@RequiredArgsConstructor
@ResponseWrapper
@SuppressWarnings("all")
public class EntityTypeController {

    private final EntityTypeService entityTypeService;

    @GetMapping("/{id}")
    public EntityType getEntityTypeById(@PathVariable("id") Long id) {
        return entityTypeService.getEntityTypeById(id);
    }

    @PostMapping
    public EntityType create(@RequestBody CreateEntityTypeRequest request) {
        return entityTypeService.create(request);
    }

    @PutMapping("/{id}")
    public EntityType update(@PathVariable("id") Long id, @RequestBody UpdateEntityTypeRequest request) {
        if (!id.equals(request.getEntityTypeId())) {
            throw new IllegalArgumentException("Path ID must match request body entityTypeId");
        }
        return entityTypeService.update(request);
    }

}
