> ## 🤔 What is this service all about?
>
> - Centralized auditing microservice for the iQ Key Value platform.
> - Passive observation: consumes business events without intrusive changes to domain services.
> - Normalizes diverse platform events into a unified, queryable audit trail.
> - Make the project easy to maintain with **8 issue templates**.
> - Quick-start documentation
> - Manage issues with **20 issue labels**.
> - Make _community healthier_ with all the guides like code of conduct, contributing, support, security...

---

# 📋 iQ Key Value Audit Service

Centralized microservice for platform-wide event consumption, transformation, and storage. Acts as the source of truth for all activity logs across the iQ Key Value ecosystem.

## About

The Audit service is the compliance and observability backbone of the platform:

- **Passive Observation** — acts as a platform-wide observer by binding to the existing `iqkv.events` exchange; requires zero code changes in domain services for basic auditing
- **Event Transformation** — maps domain-specific data (e.g., `UserEvent`, `TenantEvent`) into a generic `AuditRecord` format with enriched technical context
- **Technical Context Enrichment** — captures client IP addresses and User-Agents propagated from the Gateway to provide a complete "who, when, where" for every action
- **Storage Isolation** — maintains its own dedicated database (PostgreSQL by default), ensuring that high-volume audit logging doesn't impact business-critical transactions
- **Extensible Architecture (SPI)** — follows a provider-friendly design via `foundation-audit-spi`, allowing easy plug-in of alternative backends like Elasticsearch or custom SIEMs
- **High-Sensitivity Tracking** — consumes standard `AuditEvent` messages published by services for critical actions that don't trigger typical business events
- **Admin Search API** — provides a secured, paginated, and filterable API for Platform Administrators to review audit trails across all tenants

## Messaging & Integration

The Audit service acts as a consumer for all lifecycle events published by other platform services.

**Exchange**: `iqkv.events` (Topic)

### Consumption Bindings

| Routing Key | Source Service               | Description                                                  |
| :---------- | :--------------------------- | :----------------------------------------------------------- |
| `user.#`    | `foundation-iam-service`     | Captures user signups, profile updates, and deletions.       |
| `tenant.#`  | `foundation-iam-service`     | Tracks tenant creation, provisioning status, and suspension. |
| `billing.#` | `foundation-billing-service` | Monitors subscription changes and payment events.            |
| `audit.#`   | All Services                 | Consumes explicit high-sensitivity `AuditEvent` messages.    |

## Quick Links

- [API Documentation](./docs/api/README.md)
- [Architecture Overview](./docs/architecture/README.md)
- [Deployment Guide](./docs/deployment/README.md)
- [Contributing Guidelines](.github/CONTRIBUTING.md)

## API

Base path: `/api/v1/audits`

### Audit Log Management — `/api/v1/audits`

| Method | Path    | Auth                 | Description                                                        |
| :----- | :------ | :------------------- | :----------------------------------------------------------------- |
| `GET`  | `/`     | JWT `PLATFORM_ADMIN` | Search audit logs (paginated, filterable by user, tenant, action). |
| `GET`  | `/{id}` | JWT `PLATFORM_ADMIN` | Get detailed audit record including dynamic metadata.              |

> Auth legend: `JWT PLATFORM_ADMIN` = valid Bearer token with platform administrator authority required.

## Tech Stack

- Java 25 / Spring Boot 4.1
- MyBatis 3.x (no JPA) + PostgreSQL 17
- Liquibase for schema migrations
- RabbitMQ (passive event consumption)
- foundation-audit-model & foundation-audit-spi
- Micrometer + Prometheus
- springdoc-openapi (Swagger UI)

## Observability

The service provides comprehensive monitoring via Micrometer and Prometheus:

- **Custom Metrics**:
    - `audit.event.consumption`: Rate of events consumed by type and source service.
    - `audit.persistence.duration`: Latency of audit record storage operations.
    - `audit.search.latency`: Performance of administrative log queries.
    - `audit.storage.usage`: Volume of audit records persisted.
- **Grafana Dashboards**: Pre-configured dashboards are available in `docker/grafana/provisioning/dashboards`:
    - **JVM**: Core JVM and Spring Boot health.
    - **Audit Service**: Custom consumption and storage metrics.

## Prerequisites

- JDK 25 (Eclipse Temurin)
- Maven 3.9+
- Node.js >= 22.15.0 & pnpm >= 11.0.8 (git hooks)
- Docker & Docker Compose

## Quick Start

```bash
# Clone the repository
git clone https://github.com/IQKV/foundation-audit-service.git
cd foundation-audit-service

# Install git hooks
pnpm install

# Copy environment variables
cp .env.example .env.local
# Edit .env.local — defaults work for local Docker setup

# Start infrastructure dependencies (PostgreSQL, RabbitMQ)
docker compose up -d

# Run the service from your IDE or CLI
export $(grep -v '^#' .env.local | xargs)
./mvnw spring-boot:run -Pdev
# → API:      http://localhost:8080
# → Actuator: http://localhost:8081/actuator/health
# → Swagger:  http://localhost:8080/swagger-ui.html
```

## Environment Variables

| Variable                 | Default            | Description                                  |
| :----------------------- | :----------------- | :------------------------------------------- |
| `DB_HOST`                | `localhost`        | PostgreSQL host                              |
| `DB_PORT`                | `5432`             | PostgreSQL port                              |
| `DB_NAME`                | `auditservice`     | Database name                                |
| `DB_USERNAME`            | `svc_audit_dba`    | Database user                                |
| `DB_PASSWORD`            | `svc_audit_dba`    | Database password                            |
| `RABBITMQ_HOST`          | `localhost`        | RabbitMQ host                                |
| `RABBITMQ_PORT`          | `5672`             | RabbitMQ AMQP port                           |
| `RABBITMQ_USERNAME`      | `svc_audit_rmq`    | RabbitMQ user                                |
| `RABBITMQ_PASSWORD`      | `svc_audit_rmq`    | RabbitMQ password                            |
| `MAIL_HOST`              | `localhost`        | SMTP host                                    |
| `MAIL_PORT`              | `1025`             | SMTP port (MailHog default)                  |
| `MAIL_FROM`              | `noreply@iqkv.dev` | Sender address                               |
| `SPRING_PROFILES_ACTIVE` | `local`            | Active Spring profile                        |
| `STORAGE_TYPE`           | `postgres`         | Audit backend: `postgres` or `elasticsearch` |

> Copy `.env.example` to `.env.local` / `.env.uat` / `.env.prd` and fill in values per environment. The defaults in `.env.example` match the local Docker Compose setup — no edits needed for first-run.

## Maven Commands

```bash
# Build and test (skip Checkstyle during development)
./mvnw clean verify -Dcheckstyle.skip=true

# Run tests only
./mvnw test -Dcheckstyle.skip=true

# Explicit Checkstyle check
./mvnw checkstyle:check

# Coverage report → target/site/jacoco/index.html
./mvnw jacoco:report

# Production build
./mvnw clean package -Pproduction
```

## Docker

The project provides two Docker Compose configurations for different workflows:

### 1. Infrastructure-only (Local IDE Development)

Starts only the database and message broker. The Audit service is expected to be run from your IDE or CLI.

```bash
docker compose up -d
```

### 2. Full Stack (Containerized Development)

Starts the entire stack including the Audit service container.

```bash
# Build image
docker build -t iqkv/foundation-audit-service:latest .

# Run everything
docker compose -f compose.container.yaml up -d
```

## Monitoring

| Endpoint                   | Description                      |
| :------------------------- | :------------------------------- |
| `GET /actuator/health`     | Liveness + readiness probes      |
| `GET /actuator/info`       | Build info and platform metadata |
| `GET /actuator/metrics`    | Application metrics              |
| `GET /actuator/prometheus` | Prometheus scrape endpoint       |
| `GET /swagger-ui.html`     | API documentation                |

## Project Structure

```
src/main/java/com/iqkv/foundation/auditservice/
├── audit/            # Core Bounded Context: Records, search logic, transformation
│   ├── domain/       # AuditRecord entity, AuditStore port
│   ├── application/  # AuditLogService (orchestration), Search queries
│   └── adapter/
│       ├── in/rest/  # AuditSearchRestResource (Admin API)
│       └── out/persistence/ # MyBatis implementation of AuditStore
├── infrastructure/   # Technical concerns: RabbitMQ, Security, MyBatis config
├── shared/           # Common exceptions, utilities, value objects
└── AuditServiceApplication.java
```

## License

This project is licensed under the Apache License. See the [LICENSE](LICENSE) file for details.

## Contributing

Please read our [Contributing Guidelines](.github/CONTRIBUTING.md) and [Code of Conduct](.github/CODE_OF_CONDUCT.md).

---

## 🧩 Boilerplate Architecture

- **Passive Consumption**: Leverages Spring AMQP `@RabbitListener` with Topic wildcards to capture platform-wide events without modifying source microservices
- **Persistence**: MyBatis with XML mappers + PostgreSQL; custom `TypeHandler` for JSONB fields to store dynamic event metadata; Liquibase for schema migrations
- **Security**: Spring Security + OAuth2 Resource Server; administrative API strictly restricted to `PLATFORM_ADMIN` authority
- **SPI Pattern**: Decoupled storage logic via `foundation-audit-spi`, enabling "No Vendor Lock-in" for audit log backends
- **Observability**: Micrometer + Prometheus; structured JSON logging with Logstash encoder; health probes for Kubernetes readiness/liveness
- **Quality Tools**: Checkstyle, JaCoCo (80% gate), ArchUnit, commit convention enforcement

> See [AGENTS.md](AGENTS.md) for repository structure, DDD patterns, and agent guidelines.
