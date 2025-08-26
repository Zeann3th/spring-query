package vn.com.vds.vdt.servicebuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.com.vds.vdt.servicebuilder.controller.dto.entityType.EntityTypeCreateRequest;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;
import vn.com.vds.vdt.servicebuilder.service.common.EntityTypeService;

@RestController
@RequestMapping("/api/v1/entity-types")
@RequiredArgsConstructor
@SuppressWarnings("all")
public class EntityTypeController {

    private final EntityTypeService entityTypeService;

    @PostMapping
    public ResponseEntity<EntityType> createOrUpdate(@RequestBody EntityTypeCreateRequest request) {
        EntityType saved = entityTypeService.createOrUpdate(request);
        return ResponseEntity.ok(saved);
    }
}
