package com.orchestrationengine.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    // Shared TaskDecorator bean so the decorator can be reused/injected and instrumented easily
    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }

    // Pool 1: For "Fire-and-Forget" Async Steps (The Managers)
    @Bean(name = "workflowAsyncPool")
    public ThreadPoolTaskExecutor workflowAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-WorkerFlow-");
        executor.setTaskDecorator(mdcTaskDecorator());
        // When the queue is full and max threads are reached, run task in caller thread to apply backpressure
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // graceful shutdown: wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    // Pool 2: For Actual Step Execution logic (The Workers)
    @Bean(name = "stepExecutionPool")
    public ThreadPoolTaskExecutor stepExecutionPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("StepExec-WorkerFlow-");
        executor.setTaskDecorator(mdcTaskDecorator());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    // Simple metric exposure beans that allow other parts of the app to poll pool state
    @Bean(name = "workflowPoolMetrics")
    public PoolMetrics workflowPoolMetrics(@Qualifier("workflowAsyncPool") ThreadPoolTaskExecutor executor) {
        return new PoolMetrics(executor);
    }

    @Bean(name = "stepExecutionPoolMetrics")
    public PoolMetrics stepExecutionPoolMetrics(@Qualifier("stepExecutionPool") ThreadPoolTaskExecutor executor) {
        return new PoolMetrics(executor);
    }

    public record PoolMetrics(ThreadPoolTaskExecutor executor) {

        public int getActiveCount() {
                ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
                return pool.getActiveCount();
            }

            public int getPoolSize() {
                ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
                return pool.getPoolSize();
            }

            public int getQueueSize() {
                ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
                return pool.getQueue().size();
            }
        }
}