# ClaimGuard AI Backend

This module contains the Spring Boot backend foundation for ClaimGuard AI, a healthcare revenue cycle portfolio project focused on claim quality, denial-risk prevention, auditability, and AI-assisted review workflows.

## Current phase

- `v0.2.0` - Backend Foundation

## Purpose

This phase establishes the backend project structure, configuration model, security baseline, error-handling conventions, Flyway foundation, and initial health endpoint. It is intentionally limited to infrastructure and platform setup so later phases can add domain features cleanly.

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
- `JWT_SECRET` reserved for a future authentication phase

Optional local overrides:

- `CLAIMGUARDAI_LOCAL_DB_URL`
- `CLAIMGUARDAI_LOCAL_DB_USERNAME`
- `CLAIMGUARDAI_LOCAL_DB_PASSWORD`
- `CLAIMGUARDAI_LOCAL_DB_DRIVER`
- `CLAIMGUARDAI_LOCAL_JPA_PLATFORM`

## Health endpoint

- `GET /api/health`

Example response:

```json
{
  "status": "UP",
  "application": "ClaimGuard AI Backend",
  "version": "0.2.0",
  "environment": "local"
}
```

## What is intentionally not implemented yet

- JWT issuance or validation
- user login or seeded application users
- claim CRUD APIs
- full PostgreSQL schema
- rule engine logic
- denial risk scoring
- AI provider calls
- dashboard or reporting endpoints
- audit persistence implementation beyond structural foundations
