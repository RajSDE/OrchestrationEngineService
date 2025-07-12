package com.orchestrationengine.controller;

import com.orchestrationengine.service.WorkflowExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/serviceflow")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowExecutor workflowExecutor;

    @PostMapping(value = "/{serviceCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> runWorkflow(@PathVariable String serviceCode,
                                           @RequestBody(required = false) Map<String, Object> context,
                                           @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String language) {
        if (context == null) context = new HashMap<>();
        workflowExecutor.executeWorkflowByServiceCode(serviceCode, context, language);
        return context;
    }
}
