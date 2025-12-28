<p align="right"><img src="https://komarev.com/ghpvc/?username=RajSDE&label=Visitors&color=000000&style=flat" alt="RajSDE" />

> This `README.md` acts as a complete developer manual for your Orchestration Engine Service. It documents the architecture, configuration, adding new steps, error handling, and deployment instructions.

# Orchestration Engine Service

## 📖 Introduction

The **Orchestration Engine Service** is a lightweight, configuration-driven workflow engine designed to manage complex business processes through a centralized orchestrator. It implements the **Orchestration Pattern** with support for the **Saga Pattern** (rollback capabilities), allowing you to define, execute, and monitor sequential business workflows via XML configuration.

### Key Features

* **Centralized Control:** Decouples flow logic from business logic.
* **XML Configuration:** Define workflows (steps, order, timeouts) without code changes.
* **Distributed Tracing:** Built-in MDC logging with unique `traceId` propagation across threads.
* **Resilience:** Configurable **Retries**, **Timeouts**, and **Rollback** mechanisms.
* **Concurrency:** Non-blocking Async steps using a "Manager-Worker" thread pool architecture.
* **Thread Safety:** robust deadlock prevention using separate thread pools.

---

## 🏗 Architecture

This service follows the **Orchestrator Pattern**. A central `WorkflowExecutor` coordinates all activities.

### Core Components

1. **WorkflowController:** The entry point. Receives requests and delegates to the executor.
2. **MdcFilter:** Intercepts requests to generate/capture a unique `traceId` for observability.
3. **WorkflowExecutor:** The brain. Loads steps, manages the execution loop, handles timeouts, and triggers rollbacks.
4. **WorkflowStep (Interface):** The contract that all business logic steps must implement (`execute`, `rollback`).
5. **Thread Pools:**
* `workflowAsyncPool`: Manages async step supervision (Fire-and-forget).
* `stepExecutionPool`: Performs the actual business logic execution.



---

## 🚀 Getting Started

### Prerequisites

* Java 17+ (Recommend Java 21)
* Maven 3.8+
* Spring Boot 3.x

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/RajSDE/OrchestrationEngineService.git
cd OrchestrationEngineService

```


2. **Build the project:**
```bash
mvn clean install

```


3. **Run the application:**
```bash
mvn spring-boot:run

```



---

## ⚙️ Configuration

Workflows are defined in `src/main/resources/workflows/`. You can create a file (e.g., `user-registration.xml`) to define a new process.

### Workflow XML Structure

```xml
<workflow serviceCode="USER_REGISTRATION">
    <step id="VALIDATE_USER" 
          name="User Validation" 
          async="false" 
          retry="true" 
          rollback="true" 
          timeout="5000" 
          enable="true"/>
          
    <step id="SEND_EMAIL" 
          name="Send Welcome Email" 
          async="true" 
          retry="false" 
          rollback="false" 
          timeout="2000" 
          enable="true"/>
</workflow>

```

### Attribute Reference

| Attribute | Description | Default |
| --- | --- | --- |
| `id` | Bean name of the step (Must match `@Component("NAME")`) | **Required** |
| `async` | If `true`, runs in background. Response returns immediately. | `false` |
| `retry` | If `true`, retries 3 times on failure with 100ms backoff. | `false` |
| `rollback` | If `true`, triggers `rollback()` on failure of subsequent steps. | `false` |
| `timeout` | Max execution time in milliseconds. | `10000` |
| `enable` | Set to `false` to skip this step without removing code. | `true` |

---

## 👨‍💻 Developer Guide

### How to Add a New Step

1. **Create a Java Class:** Implement the `WorkflowStep` interface.
2. **Annotate:** Use `@Component("STEP_ID")`.
3. **Implement Logic:**

```java
package com.orchestrationengine.ums.steps;

import com.orchestrationengine.service.WorkflowStep;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component("CREATE_USER")
public class CreateUserStep implements WorkflowStep {

    @Override
    public void execute(Map<String, Object> context) {
        // 1. Get data from context
        String username = (String) context.get("username");
        
        // 2. Perform Business Logic (DB save, API call)
        System.out.println("Creating user: " + username);
        
        // 3. Save output to context for next steps
        context.put("userId", "12345");
    }

    @Override
    public void rollback(Map<String, Object> context) {
        // Compensating Transaction (Undo logic)
        String userId = (String) context.get("userId");
        System.out.println("Rolling back creation for user: " + userId);
    }
}

```

---

## 🔍 Observability & Logging

Every request is assigned a **Trace ID**. This ID is automatically injected into all logs across all threads.

**Log Format:**

```text
TIMESTAMP [THREAD_NAME] [TRACE_ID] LEVEL LOGGER - MESSAGE

```

**Example Output:**

```text
2025-12-28 10:00:00 [http-nio-8080-exec-1] [a1b2-c3d4] INFO  WorkflowExecutor - Executing step: VALIDATE_USER
2025-12-28 10:00:00 [Async-WorkerFlow-1]   [a1b2-c3d4] INFO  WorkflowExecutor - Executing async step: SEND_EMAIL
2025-12-28 10:00:01 [StepExec-WorkerFlow-1][a1b2-c3d4] INFO  SendEmailStep    - Sending email...

```

### API Response

The Trace ID is returned in the response body and headers (`X-Trace-Id`) for debugging.

```json
{
    "status": "FAILED",
    "traceId": "a1b2-c3d4-e5f6-7890",
    "error": {
        "component": "VALIDATE_USER",
        "code": "DB_CONNECTION_ERROR",
        "message": "Connection timed out"
    }
}

```

---

## 🛠 Troubleshooting

### 1. Step Not Found

* **Error:** `Bean not found for step: MY_STEP`
* **Fix:** Ensure your Java class has `@Component("MY_STEP")` and matches the XML `id`.

### 2. Async Logs Missing

* **Error:** Logs show `[null]` or missing Trace ID in async threads.
* **Fix:** Check `AsyncConfig.java`. Ensure `MdcTaskDecorator` is set on the thread pools.

### 3. Application Crashes on Null

* **Error:** `NullPointerException` in `ConcurrentHashMap`.
* **Fix:** Ensure `WorkflowExecutor` has the null-safe check for `traceId`:
```java
if (traceId == null) traceId = UUID.randomUUID().toString();

```



---

## 📜 License

This project is licensed under the MIT License.