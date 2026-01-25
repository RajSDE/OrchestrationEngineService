package com.orchestrationengine.controller;

import com.orchestrationengine.config.AsyncConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Exposes simple endpoints to read ThreadPool metrics (active threads, pool size, queue size)
 * for the application executors defined in `AsyncConfig`.
 * <p>
 * Endpoints:
 *  - GET /internal/metrics/pools => returns both pools' metrics
 *  - GET /internal/metrics/pools/workflow => workflow pool metrics
 *  - GET /internal/metrics/pools/stepExecution=> step execution pool metrics
 */
@RestController
@RequestMapping("/internal/metrics/pools")
public class PoolMetricsController {

    private final AsyncConfig.PoolMetrics workflowMetrics;
    private final AsyncConfig.PoolMetrics stepExecutionMetrics;

    public PoolMetricsController(
            @Qualifier("workflowPoolMetrics") AsyncConfig.PoolMetrics workflowMetrics,
            @Qualifier("stepExecutionPoolMetrics") AsyncConfig.PoolMetrics stepExecutionMetrics) {
        this.workflowMetrics = workflowMetrics;
        this.stepExecutionMetrics = stepExecutionMetrics;
    }

    @GetMapping
    public Map<String, Map<String, Integer>> allPools() {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        result.put("workflow", toMap(workflowMetrics));
        result.put("stepExecution", toMap(stepExecutionMetrics));
        return result;
    }

    @GetMapping("/workflow")
    public Map<String, Integer> workflowPool() {
        return toMap(workflowMetrics);
    }

    @GetMapping("/stepExecution")
    public Map<String, Integer> stepExecutionPool() {
        return toMap(stepExecutionMetrics);
    }

    private Map<String, Integer> toMap(AsyncConfig.PoolMetrics pm) {
        Map<String, Integer> m = new HashMap<>();
        m.put("activeCount", pm.getActiveCount());
        m.put("poolSize", pm.getPoolSize());
        m.put("queueSize", pm.getQueueSize());
        return m;
    }
}

