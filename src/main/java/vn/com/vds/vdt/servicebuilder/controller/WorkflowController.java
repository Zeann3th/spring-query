package vn.com.vds.vdt.servicebuilder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.com.vds.vdt.servicebuilder.common.base.ResponseWrapper;
import vn.com.vds.vdt.servicebuilder.controller.dto.workflow.TriggerWorkflowRequest;
import vn.com.vds.vdt.servicebuilder.service.core.WorkflowService;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
@SuppressWarnings("all")
@ResponseWrapper
public class WorkflowController {
    private final WorkflowService workflowService;

    @PostMapping("/{workflowName}")
    public void execute(
            @PathVariable("workflowName") String workflowName,
            @RequestBody TriggerWorkflowRequest request
    ) {
        workflowService.execute(workflowName, request);
    }
}
