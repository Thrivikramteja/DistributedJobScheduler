# Task Dispatch Engine
"A distributed, multi-threaded task dispatch engine built with Spring Boot that safely coordinates background processing across multiple worker nodes."

## Overview
Task Dispatch Engine is a high-performance system for scheduling, queuing, and processing background tasks in distributed environments. It utilizes PostgreSQL as a central state store to safely coordinate work across multiple application instances without race conditions. Out of the box, it provides priority-based scheduling, automatic retries for transient failures, and self-healing recovery for crashed worker nodes.

## Key Features
- **Distributed Locking:** Uses pessimistic row-level locking (`SELECT FOR UPDATE`) to prevent duplicate task execution.
- **Priority Queuing:** Tasks carry an integer priority; higher urgency tasks are processed first.
- **Asynchronous Execution:** Thread pool decoupling ensures the primary polling loop is never blocked.
- **Resilience & Retries:** Configurable retry limits with state transitioning to `FAILED` upon exhaustion.
- **Stale Job Recovery:** Automated background sweep detects crashed workers and re-queues abandoned tasks.
- **REST API:** Complete HTTP endpoints for submitting tasks, querying status, and early cancellation.
- **Pluggable Architecture:** Easily add new task types by implementing a single `TaskHandler` interface.
- **Observability Stack:** Fully instrumented with Micrometer, exposing detailed lifecycle metrics to Prometheus and Grafana.

## Architecture
```
                    ┌─────────────────┐
                    │   REST API      │  POST /api/jobs   GET /api/jobs/{id}   PATCH cancel
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │   PostgreSQL    │  tasks table (Flyway migrations)
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────┐
         ▼                   ▼                   ▼
   ┌───────────┐       ┌───────────┐       ┌───────────┐
   │  Worker   │       │  Worker   │       │  Worker   │  poll → claim → execute
   └───────────┘       └───────────┘       └───────────┘
         │
         ▼
   ┌───────────┐       ┌───────────┐
   │   Redis   │       │ Prometheus│
   └───────────┘       └───────────┘
```
- **API Layer**: Accepts HTTP requests from clients to schedule tasks or poll for status.
- **PostgreSQL**: Serves as the single source of truth for task states, payloads, and locking coordination.
- **Worker Nodes**: Application instances that continuously poll the database, claim tasks, and execute them in a fixed thread pool.
- **Observability Stack**: Prometheus scrapes metrics from the app instances, visualizing them in Grafana.
- **Task Lifecycle**: `PENDING` -> `RUNNING` -> `COMPLETED` / `FAILED` / `CANCELLED`

## Design Decisions
- **Preventing Duplicate Execution**: Implemented pessimistic row-level locking to guarantee that even if multiple workers poll simultaneously, a task is locked and claimed exclusively by one instance.
- **Handling Worker Crashes**: Designed a `StaleJobRecoveryService` that runs periodically to find `RUNNING` tasks older than 15 minutes, resetting them to `PENDING` to ensure 100% completion guarantees.
- **Execution Decoupling**: Separated the database polling loop from actual task execution by using a `ThreadPoolTaskExecutor`, ensuring slow network calls in tasks don't block the scheduler.
- **Why PostgreSQL as a Queue**: Chose to use Postgres over a dedicated broker (like RabbitMQ) to reduce infrastructure complexity and leverage ACID guarantees for state transitions, accepting a slight trade-off in extreme-scale throughput.
- **Extensibility**: Adopted a Strategy pattern via the `TaskHandler` interface, allowing developers to drop in new task types dynamically via Spring's `@Component` scanning.

## Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.2
- **Database**: PostgreSQL 16
- **Cache / Extensions**: Redis 7
- **Migrations**: Flyway
- **Observability**: Micrometer, Prometheus, Grafana
- **Infrastructure**: Docker & Docker Compose

## Getting Started
### Prerequisites
- Docker and Docker Compose

### Run via Docker
To boot the entire stack (Application, PostgreSQL, Redis, Prometheus, Grafana) in under 2 minutes:
1. Clone the repository
2. Run the compose stack:
```bash
docker compose up -d --build
```
3. Check the application health:
```bash
curl -s http://localhost:8080/actuator/health
```

## API Reference
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/jobs` | Submit a new task |
| `GET` | `/api/jobs/{id}` | Get task by UUID |
| `PATCH`| `/api/jobs/{id}/cancel` | Cancel a `PENDING` task |

**Sample Request (Create Task):**
```bash
curl -X POST http://localhost:8080/api/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "type": "LOG_MESSAGE",
    "payload": "Hello World",
    "priority": 10
  }'
```

**Sample Response:**
```json
{
  "jobId": "b1f2c3d4-...",
  "status": "PENDING"
}
```

## Testing
Run the test suite using Gradle:
```bash
./gradlew test
```
The suite includes:
- **Unit Tests**: Validating service-layer logic, retry exhaustion, and cancellation edge cases using Mockito.
- **Integration Tests**: Validating full HTTP round-trips and repository layer functionality against an in-memory H2 database.

## License
Distributed under the MIT License.


