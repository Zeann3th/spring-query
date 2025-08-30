package vn.com.vds.vdt.servicebuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import vn.com.vds.vdt.servicebuilder.common.base.ResponseWrapper;
import vn.com.vds.vdt.servicebuilder.controller.dto.instance.InstanceRequest;
import vn.com.vds.vdt.servicebuilder.entity.Instance;
import vn.com.vds.vdt.servicebuilder.service.common.InstanceService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/entities/{entityName}")
@RequiredArgsConstructor
@ResponseWrapper
@SuppressWarnings("all")
public class InstanceController {

    private final InstanceService instanceService;

    @GetMapping
    public Page<Instance> getInstances(
            @PathVariable("entityName") String entityName,
            Pageable pageable
    ) {
        return instanceService.getInstances(entityName, pageable);
    }

    @GetMapping("/{id}")
    public Instance getInstanceById(
            @PathVariable("entityName") String entityName,
            @PathVariable("id") Long id
    ) {
        return instanceService.getInstanceById(id);
    }

    @PostMapping
    public Map<String, Long> create(
            @PathVariable("entityName") String entityName,
            @RequestBody InstanceRequest request
    ) {
        Long instanceId = instanceService.createInstance(entityName, request.getAttributes());
        return Map.of("id", instanceId);
    }

    @PutMapping("/{id}")
    public void update(
            @PathVariable("entityName") String entityName,
            @PathVariable("id") Long id,
            @RequestBody InstanceRequest request
    ) {
        instanceService.updateInstance(id, request.getAttributes());
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable("entityName") String entityName,
            @PathVariable("id") Long id
    ) {
        instanceService.deleteInstance(id);
    }
}
