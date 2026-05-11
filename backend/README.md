# ClaimGuard AI Backend

Spring Boot backend for the ClaimGuard AI portfolio project.

## Current phase

- `v0.9.0` - Production Readiness, Security Hardening, and Deployment

## Scope of this phase

This phase hardens the existing authenticated API without adding frontend work, external healthcare integrations, OCR, uploads, admin dashboards, real AI provider calls, or new business workflows.

Implemented in this phase:

- explicit production profile configuration
- environment-driven JWT secret requirements for production
- safer production defaults for error handling and shutdown behavior
- security response headers and frontend-ready CORS handling
- deployment artifacts for containerized runs
- updated tests for validation, CORS, headers, auth, and production config safety

## Tech stack

- Java 21
- Spring Boot 3.3
- Spring Web
- Spring Validation
- Spring Data JPA
- Spring Security
- Flyway
- PostgreSQL
- H2 for local and test profiles
- Maven

## Profiles

- `local`: H2-backed, seeded demo user enabled, developer-friendly JWT default
- `test`: H2-backed, seeded test user enabled, fixed test JWT secret
- `prod`: PostgreSQL-oriented, seed user disabled, `JWT_SECRET` required

## Local run

From `backend/`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Default local credentials:

- username: `local.analyst`
- password: `LocalPass123!`

## Production-style run

From `backend/`:

```bash
$env:SPRING_PROFILES_ACTIVE="prod"
$env:JWT_SECRET="replace-with-a-strong-secret-at-least-32-characters"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/claimguardai"
$env:SPRING_DATASOURCE_USERNAME="claimguardai"
$env:SPRING_DATASOURCE_PASSWORD="change-me"
mvn spring-boot:run
```

The production profile refuses startup when:

- `JWT_SECRET` is missing
- `JWT_SECRET` is shorter than 32 characters
- a known local or test placeholder secret is used
- the local seed user is enabled
- CORS uses `*`

## Supported environment variables

Required for `prod`:

- `SPRING_PROFILES_ACTIVE=prod`
- `JWT_SECRET`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Optional:

- `SERVER_PORT` default `8080`
- `CORS_ALLOWED_ORIGINS` comma-separated explicit origins
- `JWT_EXPIRATION_MINUTES` default `60`

Backward-compatible non-prod datasource and local seed overrides are still supported:

- `CLAIMGUARDAI_DB_URL`
- `CLAIMGUARDAI_DB_USERNAME`
- `CLAIMGUARDAI_DB_PASSWORD`
- `CLAIMGUARDAI_LOCAL_DB_URL`
- `CLAIMGUARDAI_LOCAL_DB_USERNAME`
- `CLAIMGUARDAI_LOCAL_DB_PASSWORD`
- `CLAIMGUARDAI_LOCAL_DB_DRIVER`
- `CLAIMGUARDAI_LOCAL_JPA_PLATFORM`
- `CLAIMGUARDAI_LOCAL_SEED_USER_ENABLED`
- `CLAIMGUARDAI_LOCAL_SEED_USER_USERNAME`
- `CLAIMGUARDAI_LOCAL_SEED_USER_EMAIL`
- `CLAIMGUARDAI_LOCAL_SEED_USER_PASSWORD`

## Container build and run

Build from `backend/`:

```bash
docker build -t claimguardai-backend:0.9.0 .
```

Run:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SECRET=replace-with-a-strong-secret-at-least-32-characters \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/claimguardai \
  -e SPRING_DATASOURCE_USERNAME=claimguardai \
  -e SPRING_DATASOURCE_PASSWORD=change-me \
  -e CORS_ALLOWED_ORIGINS=http://localhost:5173 \
  claimguardai-backend:0.9.0
```

## API access

Public:

- `GET /api/health`
- `POST /api/auth/login`

Protected:

- `GET /api/auth/me`
- `POST /api/claims`
- `GET /api/claims`
- `GET /api/claims/{claimId}`
- `PATCH /api/claims/{claimId}/status`
- `POST /api/claims/{claimId}/review-notes`
- `GET /api/claims/{claimId}/review-notes`
- `POST /api/claims/{claimId}/analyze`
- `GET /api/claims/{claimId}/analysis/latest`
- `GET /api/claims/{claimId}/analysis/history`
- `GET /api/dashboard/summary`

All protected endpoints remain owner-scoped.

## Quick API examples

Health:

```bash
curl http://localhost:8080/api/health
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"local.analyst\",\"password\":\"LocalPass123!\"}"
```

Current user:

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <jwt>"
```

Create claim:

```bash
curl -X POST http://localhost:8080/api/claims \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d "{\"claimNumber\":\"CLM-10001\",\"payerName\":\"Acme Health Plan\",\"providerName\":\"North Valley Clinic\",\"serviceDate\":\"2026-05-01\",\"billedAmount\":1250.75}"
```

## Security notes

- JWT tokens are required for `/api/**` except the public health and login endpoints.
- Correlation IDs are preserved through request and error responses via `X-Correlation-Id`.
- Production responses do not expose stack traces.
- The backend does not log request bodies, JWT tokens, Authorization headers, passwords, or password hashes.
