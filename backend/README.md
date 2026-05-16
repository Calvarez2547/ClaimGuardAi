# ClaimGuard AI Backend

Spring Boot backend for the ClaimGuard AI portfolio project.

## Version

- `v1.1.0`

## Scope

This backend demonstrates authenticated claim intake, review workflow support, deterministic scoring, AI-assisted claim analysis summaries, dashboard summaries, and production-style security/configuration practices. It intentionally does not include a frontend, external payer/EHR/clearinghouse integrations, or PHI-facing production claims.

## Tech stack

- Java 21
- Spring Boot 3.3
- Spring Web
- Spring Validation
- Spring Data JPA
- Spring Security
- Flyway
- PostgreSQL
- H2 for local and test
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

Optional local AI provider variables:

- `AI_ENABLED=false` by default
- `AI_PROVIDER=OPENAI`
- `AI_API_KEY` required only when `AI_ENABLED=true`
- `AI_MODEL=gpt-4o-mini`
- `AI_TIMEOUT_SECONDS=30`

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

## Environment variables

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
- `CLAIMGUARDAI_RUNTIME_ENV`
- `AI_ENABLED`
- `AI_PROVIDER`
- `AI_API_KEY`
- `AI_MODEL`
- `AI_TIMEOUT_SECONDS`

Local and test profile overrides remain available:

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
docker build -t claimguardai-backend:1.1.0 .
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
  claimguardai-backend:1.1.0
```

## Endpoint summary

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

All protected endpoints are owner-scoped.

## Notes

- The health endpoint exposes application name, version, and runtime environment.
- Deterministic risk scoring remains the source of truth even when AI is enabled.
- If AI is disabled or the provider call fails, analysis falls back safely to the deterministic backend summary.
- This project is not presented as HIPAA compliant, production-ready for PHI, or integrated with a real payer, EHR, or clearinghouse.
- The frontend directory remains reserved for future work.

See the root [README.md](../README.md), [docs/api/README.md](../docs/api/README.md), and [docs/demo/DEMO_FLOW.md](../docs/demo/DEMO_FLOW.md).
