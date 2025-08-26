package vn.com.vds.vdt.servicebuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.vds.vdt.servicebuilder.controller.dto.instance.InstanceCreateRequest;
import vn.com.vds.vdt.servicebuilder.controller.dto.instance.InstanceResponse;
import vn.com.vds.vdt.servicebuilder.controller.dto.instance.InstanceUpdateRequest;
import vn.com.vds.vdt.servicebuilder.entity.Instance;
import vn.com.vds.vdt.servicebuilder.service.common.InstanceService;

@RestController
@RequestMapping("/api/v1/instances")
@RequiredArgsConstructor
@SuppressWarnings("all")
public class InstanceController {

    private final InstanceService instanceService;

    @PostMapping
    public ResponseEntity<InstanceResponse> create(@RequestBody InstanceCreateRequest request) {
        Instance saved = instanceService.createInstance(request.getEntityType(), request.getAttributes());
        return ResponseEntity.ok(InstanceResponse.builder().id(saved.getEntityId()).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstanceResponse> update(@PathVariable("id") Long id,
            @RequestBody InstanceUpdateRequest request) {
        Instance saved = instanceService.updateInstance(id, request.getAttributes());
        return ResponseEntity.ok(InstanceResponse.builder().id(saved.getEntityId()).build());
    }
}
