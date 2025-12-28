package com.orchestrationengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    // Pool 1: For "Fire-and-Forget" Async Steps (The Managers)
    @Bean(name = "workflowAsyncPool")
    public ThreadPoolTaskExecutor workflowAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setThreadNamePrefix("Async-WorkerFlow-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    // Pool 2: For Actual Step Execution logic (The Workers)
    @Bean(name = "stepExecutionPool")
    public ThreadPoolTaskExecutor stepExecutionPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); // Needs to be larger to handle blocking
        executor.setMaxPoolSize(100);
        executor.setThreadNamePrefix("StepExec-WorkerFlow-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }
}