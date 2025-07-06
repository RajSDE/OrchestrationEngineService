# Orchestration Engine Service

## Overview

**Orchestration Engine Service** is a production-grade, extensible Spring Boot microservice for declarative workflow orchestration. It allows you to define business workflows in XML files, where each step is mapped to a pluggable Java service. The engine supports dynamic workflow execution, retry logic, rollback, and asynchronous step execution.

---

## Key Features

- **Declarative Workflow Definition:** Workflows are defined in XML files, mapping step IDs to service handlers.
- **Dynamic Step Resolution:** Steps are resolved at runtime using Spring's ApplicationContext.
- **Retry Logic:** Steps can be retried on failure, with configurable retry counts.
- **Rollback Support:** Steps can implement rollback logic for compensating transactions.
- **Async Execution:** Steps can be executed asynchronously using an ExecutorService.
- **Extensible:** Add new steps by implementing the `WorkflowStep` interface and registering them in XML.
- **REST API:** Trigger workflows via REST endpoints, with service code-based routing.

---

## Project Structure

```
com.orchestrationengine
├── config
│   └── WorkflowStepLoader.java         # Loads workflow steps from XML
├── controller
│   └── WorkflowController.java         # REST API for workflow execution
├── exception                           # (Custom exceptions)
├── model                               # (Domain models)
├── repository                          # (Persistence layer)
├── service
│   ├── WorkflowStep.java               # Step interface (with rollback)
│   ├── WorkflowExecutor.java           # Orchestrates workflow execution
│   ├── ValidateUserService.java        # Example step implementation
│   ├── CheckBalanceService.java        # Example step implementation
│   ├── DebitAccountService.java        # Example step implementation
│   └── SendNotificationService.java    # Example step implementation
└── OrchestrationEngineServiceApplication.java
```

---

## Workflow XML Example

A workflow is defined in `src/main/resources/workflow-steps.xml`:

```xml
<workflow id="PAYMENT_FLOW">
  <step id="VALIDATE_USER" handler="com.orchestrationengine.service.ValidateUserService"/>
  <step id="CHECK_BALANCE" handler="com.orchestrationengine.service.CheckBalanceService"/>
  <step id="DEBIT_ACCOUNT" handler="com.orchestrationengine.service.DebitAccountService"/>
  <step id="SEND_NOTIFICATION" handler="com.orchestrationengine.service.SendNotificationService"/>
</workflow>
```

- Each `<step>` references a Spring bean implementing `WorkflowStep`.
- The `workflow id` attribute is used as the `service code` in the API.

---

## How It Works

1. **Define Steps:** Implement the `WorkflowStep` interface for each business step. Optionally override `rollback()`.
2. **Configure Workflow:** Add your workflow XML in `src/main/resources/` with a unique `id`.
3. **Trigger Workflow:** Call the REST API:
   - `POST /v1/serviceflow/{serviceCode}`
   - The engine loads the workflow by `serviceCode`, executes steps in order, and manages retry/rollback/async as configured.

---

## API Usage

- **Endpoint:** `POST /v1/serviceflow/{serviceCode}`
- **Request Body:** JSON map (context for workflow)
- **Response:** Success or error message

---

## Extending the Engine

- **Add a New Step:**
  1. Create a class implementing `WorkflowStep`.
  2. Annotate with `@Component("STEP_ID")`.
  3. Add a `<step>` in your workflow XML.
- **Add a New Workflow:**
  1. Create a new XML file in `resources/` with a unique `id`.
  2. Trigger via `/v1/serviceflow/{id}`.

---

## Advanced Features

- **Retry Logic:** Steps can be retried on failure (configurable per step/workflow).
- **Rollback:** If a step fails, previous steps' `rollback()` methods are called in reverse order.
- **Async Steps:** Steps can be marked/executed asynchronously using an ExecutorService.

---

## Best Practices

- Keep steps stateless for thread safety.
- Use DTOs for context if needed.
- Validate workflow XMLs with XSD if required.
- Log all workflow executions for audit.

---

## Getting Started

1. Clone the repo.
2. Build with Maven: `mvn clean install`
3. Run: `mvn spring-boot:run`
4. Test the API with your workflow XML and context.

---

## License

This project is proprietary and all code is owned by RajSDE (GitHub: RajSDE) (c) 2025. All rights reserved.

- No part of this codebase may be used, copied, modified, or distributed for commercial purposes without explicit written permission from the owner.
- Personal, non-commercial use is permitted with attribution.
- For full terms, see the LICENSE file in this repository.
