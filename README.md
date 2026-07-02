# ClaimGuard AI

ClaimGuard AI is a backend-focused healthcare revenue cycle portfolio project for reviewing claim quality before submission. It demonstrates a production-style Spring Boot API with authenticated claim intake, review workflow endpoints, deterministic risk scoring, analysis history, and dashboard summaries without pretending to be a real payer, EHR, clearinghouse, or PHI-integrated platform.

## Why it exists

Manual claim review is repetitive, inconsistent, and hard to audit. This project shows how a backend can:

- capture claims in a structured way
- enforce authenticated, owner-scoped access
- record review workflow activity
- surface deterministic administrative risk signals
- provide a clear demoable API for technical review

The result is a portfolio-grade backend that is easy to inspect on GitHub, easy to run locally, and straightforward to walk through in an interview or classroom setting.

## Current status

- Current version: `v1.1.0`
- Current phase: `Phase 11 - Real AI Provider Integration`
- Backend baseline through `v1.1.0` is complete
- The `frontend/` directory now contains a React/TypeScript MVP for local portfolio demonstration

## What is implemented

- JWT authentication with a local/test seeded user path
- current-user endpoint for authenticated identity checks
- claim creation, listing, and detail retrieval
- owner-scoped claim status updates
- owner-scoped review note creation and retrieval
- deterministic claim analysis with persisted history
- optional OpenAI-backed claim review narrative generation with safe fallback
- structured risk scoring breakdowns and recommended actions
- owner-scoped dashboard summary metrics
- health endpoint, structured errors, and correlation IDs
- local H2 support, PostgreSQL-oriented production profile, Flyway migrations, and container build support

## What is not implemented

- no production frontend deployment in this phase
- no OCR, uploads, email, payments, payer connectivity, EHR connectivity, or clearinghouse connectivity
- no production-ready PHI workflow or HIPAA compliance claim
- no admin dashboard or global cross-tenant reporting

## Backend stack

- Java 21
- Spring Boot 3.3
- Spring Web
- Spring Validation
- Spring Data JPA
- Spring Security
- Flyway
- PostgreSQL for production-style configuration
- H2 for local and test profiles
- Maven

## Security model

- deny-by-default security posture for protected API routes
- Bearer JWT authentication for all protected endpoints
- owner scoping for claims, review notes, analyses, and dashboard summaries
- structured `401`, `403`, `404`, and `400` responses
- correlation ID propagation through request handling and errors via `X-Correlation-Id`
- hardened response headers and environment-based CORS configuration
- production profile startup validation for JWT secret strength and explicit CORS origins
- AI provider configuration validation when AI is enabled
- local seeded user disabled by default in `prod`

This is a production-style security baseline for a portfolio backend. It is not presented as certified HIPAA compliance, production PHI handling, or a real healthcare system integration.

## API summary

Public endpoints:

- `GET /api/health`
- `POST /api/auth/login`

Protected endpoints:

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

Detailed request and response examples live in [docs/api/README.md](docs/api/README.md).

## Repository layout

- `backend/` Spring Boot API
- `docs/` supporting project documentation
- `database/` database reference folders
- `scripts/` helper scripts
- `frontend/` React/TypeScript local MVP

## Local run

Prerequisites:

- Java 21
- Maven 3.9+
- Node.js and npm for the frontend

From [`backend/`](backend/):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The default local profile uses in-memory H2 and seeds one demo user:

- username: `local.analyst`
- password: `LocalPass123!`

The API starts on `http://localhost:8080` by default.

From [`frontend/`](frontend/):

```bash
npm install
npm run dev
```

The frontend starts on `http://localhost:5173` and expects:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Tests

From `backend/`:

```bash
mvn test
```

The current suite covers:

- authentication
- claim intake and owner scoping
- claim lifecycle review endpoints
- claim analysis and persisted history
- AI provider success, failure fallback, and configuration validation
- dashboard summary behavior
- health endpoint and hardened headers
- production configuration validation
- deterministic risk scoring

## Required environment variables

Local defaults exist, but these are the important runtime variables:

For `local`:

- `JWT_SECRET` optional because a local default exists
- `SERVER_PORT` optional, default `8080`
- `AI_ENABLED` optional, default `false`
- `AI_PROVIDER` optional, default `OPENAI`
- `AI_API_KEY` required only when `AI_ENABLED=true`
- `AI_MODEL` optional, default `gpt-4o-mini`
- `AI_TIMEOUT_SECONDS` optional, default `30`

For `prod`:

- `SPRING_PROFILES_ACTIVE=prod`
- `JWT_SECRET` required and must be at least 32 characters
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Optional:

- `CORS_ALLOWED_ORIGINS` comma-separated explicit origins
- `JWT_EXPIRATION_MINUTES`
- `CLAIMGUARDAI_RUNTIME_ENV`

More setup detail is in [docs/setup/LOCAL_SETUP.md](docs/setup/LOCAL_SETUP.md) and [docs/deployment/README.md](docs/deployment/README.md).

## Demo flow

The cleanest recruiter-friendly walkthrough is:

1. start the backend with the `local` profile
2. start the frontend with `npm run dev`
3. open `http://localhost:5173`
4. log in with the seeded local user
5. view the dashboard summary
6. create a fake/demo claim
7. list claims and fetch claim details
8. update status, add notes, and run claim analysis
9. view latest analysis and analysis history
10. show logout and protected route behavior

Use [docs/demo/DEMO_FLOW.md](docs/demo/DEMO_FLOW.md) for the exact commands.

## Portfolio / recruiter summary

ClaimGuard AI is best reviewed as a backend engineering project. The strongest signals are:

- clean Spring Boot module organization
- authenticated and owner-scoped REST endpoints
- deterministic scoring logic with test coverage
- optional real OpenAI provider integration with explicit fallback behavior
- structured error handling and request correlation
- production-style profile validation and deployment posture
- honest boundaries around what is simulated versus truly integrated
- explicit non-claims around HIPAA, PHI readiness, payer connectivity, and clinical/legal decision making

## Roadmap / phase history

- `v0.1.0`: repository and documentation foundation
- `v0.2.0`: backend foundation
- `v0.3.0`: authentication and user foundation
- `v0.4.0`: claim intake foundation
- `v0.5.0`: claim lifecycle and review foundation
- `v0.6.0`: claim analysis foundation
- `v0.7.0`: deterministic risk scoring foundation
- `v0.8.0`: dashboard summary foundation
- `v0.9.0`: production readiness, security hardening, and deployment
- `v1.0.0 release candidate`: final polish, demo flow, release docs, and portfolio readiness
- `v1.1.0`: real OpenAI provider integration with deterministic scoring retained as source of truth

Release notes: [docs/releases/v1.1.0.md](docs/releases/v1.1.0.md)
