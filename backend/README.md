# ClaimGuard AI Backend

This module contains the Spring Boot backend foundation for ClaimGuard AI, a healthcare revenue cycle portfolio project focused on claim quality, denial-risk prevention, auditability, and AI-assisted review workflows.

## Current phase

- `v0.3.0` - Authentication and User Foundation

## Purpose

This phase establishes the authentication and user identity foundation on top of the backend baseline. It adds a minimal user model, role support, password hashing, JWT-based authentication, current-user lookup, and local or test bootstrap support without adding product workflows yet.

## Tech stack

- Java 21 target
- Spring Boot
- Spring Web
- Spring Validation
- Spring Data JPA
- Spring Security
- Flyway
- PostgreSQL driver
- H2 for local or test profile bootstrapping and automated tests
- Maven

## How to run locally

1. Ensure Java 21+ and Maven are installed.
2. From the `backend/` directory, run:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The `local` profile uses an in-memory H2 datasource by default so the backend can start without an external PostgreSQL instance during this phase.

To run against PostgreSQL instead, override the local datasource properties with PostgreSQL values or change the active profile strategy for your environment.

## Required environment variables

These are supported now, even though some are placeholders for later phases:

- `SERVER_PORT` optional, defaults to `8080`
- `CLAIMGUARDAI_DB_URL` PostgreSQL URL for the default profile
- `CLAIMGUARDAI_DB_USERNAME` PostgreSQL username for the default profile
- `CLAIMGUARDAI_DB_PASSWORD` PostgreSQL password for the default profile
- `CLAIMGUARDAI_CORS_ALLOWED_ORIGINS` comma-separated allowed frontend origins
- `AI_API_KEY` reserved for a future AI integration phase
- `JWT_SECRET` JWT signing secret for non-local environments, minimum 32 characters
- `JWT_EXPIRATION_MINUTES` optional token lifetime override, defaults to `60`

Optional local overrides:

- `CLAIMGUARDAI_LOCAL_DB_URL`
- `CLAIMGUARDAI_LOCAL_DB_USERNAME`
- `CLAIMGUARDAI_LOCAL_DB_PASSWORD`
- `CLAIMGUARDAI_LOCAL_DB_DRIVER`
- `CLAIMGUARDAI_LOCAL_JPA_PLATFORM`
- `CLAIMGUARDAI_LOCAL_SEED_USER_ENABLED`
- `CLAIMGUARDAI_LOCAL_SEED_USER_USERNAME`
- `CLAIMGUARDAI_LOCAL_SEED_USER_EMAIL`
- `CLAIMGUARDAI_LOCAL_SEED_USER_PASSWORD`

The local profile also provides a default JWT secret and a default seeded user so the phase can be exercised without extra setup.

## Authentication endpoints

- `POST /api/auth/login`
- `GET /api/auth/me`

Example login request:

```json
{
  "username": "local.analyst",
  "password": "LocalPass123!"
}
```

Example login response shape:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresAt": "2026-05-07T00:00:00Z"
}
```

Current-user response shape:

```json
{
  "id": 1,
  "username": "local.analyst",
  "email": "local.analyst@claimguardai.local",
  "roles": [
    "USER"
  ]
}
```

## Public health endpoint

- `GET /api/health`

Example response:

```json
{
  "status": "UP",
  "application": "ClaimGuard AI Backend",
  "version": "0.3.0",
  "environment": "local"
}
```

## What is intentionally not implemented yet

- claim CRUD APIs
- full PostgreSQL schema
- rule engine logic
- denial risk scoring
- AI provider calls
- dashboard or reporting endpoints
- audit persistence implementation beyond structural foundations
- admin user management workflows
