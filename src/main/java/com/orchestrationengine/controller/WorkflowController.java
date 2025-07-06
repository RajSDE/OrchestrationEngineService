package com.orchestrationengine.controller;

import com.orchestrationengine.service.WorkflowExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/v1/serviceflow")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowExecutor executor;

    @PostMapping("/{serviceCode}")
    public ResponseEntity<?> runWorkflow(@PathVariable String serviceCode, @RequestBody Map<String, Object> context) {
        executor.executeWorkflowByServiceCode(serviceCode, context);
        return ResponseEntity.ok(context);
    }
}
