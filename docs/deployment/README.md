## 📜 Deployment Guide

This document covers the three main deployment scenarios for the Audit Service: local IDE development, local containerized development, and production.

---

## Prerequisites

| Tool           | Minimum Version | Notes                                        |
| :------------- | :-------------- | :------------------------------------------- |
| JDK            | 25 (Temurin)    | Required for local IDE run                   |
| Maven          | 3.9+            | Or use the included `./mvnw`                 |
| Docker         | 24+             | Required for all compose setups              |
| Docker Compose | 2.20+           | `compose.yaml` uses `extends`                |
| Node.js        | 22.15.0+        | Required only for git hooks (`pnpm install`) |
| pnpm           | 11.0.8+         | Required only for git hooks                  |

---

## Environment Setup

All runtime credentials and connection details are driven by environment variables. Defaults in `application.yml` match the local Docker Compose setup, so no changes are needed for first-run.

```bash
# Copy the template — defaults work out of the box for local Docker
cp .env.example .env.local
```

The full list of variables is documented in the [Environment Variables](../../README.md#environment-variables) section of the root README.

> **Precedence**: `.env.local` overrides `.env`. If neither file is present, variables must be provided via shell environment (e.g., in CI/CD).

---

## Scenario 1 — Local IDE Development

Starts only the infrastructure (PostgreSQL, RabbitMQ, MailHog, DbGate, monitoring). The service itself runs from your IDE or CLI with hot-reload via Spring DevTools.

```bash
# Start infrastructure
cp .env.example .env.local
docker compose up -d

# Run the service (activates local Spring profile + DevTools)
./mvnw spring-boot:run -Plocal
```

The `local` Spring profile (`application-local.yml`) enables:

- Liquibase with `demo` context (seed data)
- All Actuator endpoints exposed
- Debug logging for `com.iqkv`
- RabbitMQ messaging enabled

**Service endpoints:**

| Endpoint                                | Description              |
| :-------------------------------------- | :----------------------- |
| `http://localhost:8080/swagger-ui.html` | Swagger UI               |
| `http://localhost:8081/actuator`        | Actuator (all endpoints) |
| `http://localhost:5432`                 | PostgreSQL               |
| `http://localhost:15672`                | RabbitMQ Management UI   |
| `http://localhost:3100`                 | DbGate (DB admin)        |
| `http://localhost:8025`                 | MailHog Web UI           |
| `http://localhost:9090`                 | Prometheus               |
| `http://localhost:3000`                 | Grafana                  |

---

## Scenario 2 — Full Containerized Stack

Builds and runs the entire stack including the Audit Service container. Useful for integration testing or demo environments.

```bash
cp .env.example .env.local

# Build the image
docker build -t iqkv/auditservice:dev .

# Start everything
docker compose -f compose.container.yaml up -d

# Follow service logs
docker compose -f compose.container.yaml logs -f auditservice
```

The container uses the `builder` stage of the multi-stage `Dockerfile`. It starts with the `local` Spring profile unless `SPRING_PROFILES_ACTIVE` is overridden in `.env.local`.

To stop and clean up:

```bash
docker compose -f compose.container.yaml down -v
```

---

## Scenario 3 — Production Build

```bash
# Create a production-optimised JAR
./mvnw clean package -Pproduction -DskipTests

# Build the production image (uses the runtime stage — non-root user, JRE only)
docker build -t iqkv/foundation-audit-service:latest .
```

The production `Dockerfile` stage:

- Uses `eclipse-temurin:25-jre-alpine` (JRE only, ~80 MB smaller than JDK)
- Runs as non-root user `appuser` (UID 1001)
- Applies layered JAR extraction for optimal layer caching
- Exposes ports `8080` (API) and `8081` (Actuator/management)

In production, supply all environment variables via your secrets manager or CI/CD platform. Do **not** commit `.env.prd` to version control.

**Minimum required variables for production:**

```
DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD
RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_USERNAME, RABBITMQ_PASSWORD
SPRING_PROFILES_ACTIVE=prd
JWT_PUBLIC_KEY_PATH
ROLLOUT_MODE=MULTI_TENANT
```

---

## Health & Readiness Probes

| Probe     | Endpoint                                              |
| :-------- | :---------------------------------------------------- |
| Liveness  | `GET http://localhost:8081/actuator/health/liveness`  |
| Readiness | `GET http://localhost:8081/actuator/health/readiness` |

Both are pre-configured in the `Dockerfile` `HEALTHCHECK` directive and in `compose.base.yaml` for the `auditservice` container.

---

## Database Migrations

Schema migrations are managed by Liquibase. In the `local` profile, migrations run automatically on startup with the `demo` context (includes seed data). In other profiles, Liquibase is disabled by default and must be enabled explicitly:

```yaml
spring:
    liquibase:
        enabled: true
        contexts: production # omit 'demo' context in production
```

---

## Maven Build Reference

```bash
# Development build (skip Checkstyle for speed)
./mvnw clean verify -Dcheckstyle.skip=true

# Run tests only
./mvnw test -Dcheckstyle.skip=true

# Style check (run before committing)
./mvnw checkstyle:check

# Coverage report → target/site/jacoco/index.html
./mvnw jacoco:report

# Production JAR
./mvnw clean package -Pproduction
```
