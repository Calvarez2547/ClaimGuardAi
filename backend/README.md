# ClaimGuard AI Backend

This module contains the Spring Boot backend foundation for ClaimGuard AI, a healthcare revenue cycle portfolio project focused on claim quality, denial-risk prevention, auditability, and AI-assisted review workflows.

## Current phase

- `v0.6.0` - Claim AI Analysis Foundation

## Purpose

This phase adds the first owner-scoped claim AI analysis foundation on top of authenticated claim lifecycle and review. It supports deterministic backend-owned risk findings, persistent analysis history, and a safe internal fallback summary without adding dashboard APIs, admin workflows, document upload, OCR, external integrations, real AI provider calls, or frontend work.

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

## Claim intake endpoints

All claim endpoints require a valid Bearer token.

- `POST /api/claims`
- `GET /api/claims/{claimId}`
- `GET /api/claims`
- `PATCH /api/claims/{claimId}/status`
- `POST /api/claims/{claimId}/review-notes`
- `GET /api/claims/{claimId}/review-notes`

Example create-claim request:

```json
{
  "claimNumber": "CLM-10001",
  "patientControlNumber": "PCN-1001",
  "payerName": "Acme Health Plan",
  "providerName": "North Valley Clinic",
  "serviceDate": "2026-05-01",
  "billedAmount": 1250.75,
  "priorAuthRequired": false,
  "priorAuthNumber": null,
  "claimNotes": "Initial clean claim intake."
}
```

Example claim response:

```json
{
  "id": 1,
  "claimNumber": "CLM-10001",
  "patientControlNumber": "PCN-1001",
  "payerName": "Acme Health Plan",
  "providerName": "North Valley Clinic",
  "serviceDate": "2026-05-01",
  "billedAmount": 1250.75,
  "priorAuthRequired": false,
  "priorAuthNumber": null,
  "claimStatus": "RECEIVED",
  "claimNotes": "Initial clean claim intake.",
  "createdByUserId": 1,
  "createdAt": "2026-05-08T00:00:00Z",
  "updatedAt": "2026-05-08T00:00:00Z"
}
```

Claim records are scoped to the authenticated user. Requests for another user's claim return the same structured not-found response as requests for a missing claim.

Example status update request:

```json
{
  "status": "IN_REVIEW"
}
```

Example review note request:

```json
{
  "noteText": "Reviewed claim details and flagged missing provider information."
}
```

Example review note response:

```json
{
  "id": 1,
  "claimId": 1,
  "noteText": "Reviewed claim details and flagged missing provider information.",
  "createdAt": "2026-05-08T00:00:00Z",
  "updatedAt": "2026-05-08T00:00:00Z"
}
```

## Claim analysis endpoints

All analysis endpoints require a valid Bearer token and are scoped to the authenticated claim owner.

- `POST /api/claims/{claimId}/analyze`
- `GET /api/claims/{claimId}/analysis/latest`
- `GET /api/claims/{claimId}/analysis/history`

Example analysis response:

```json
{
  "analysisId": 1,
  "claimId": 1,
  "riskScore": 45,
  "riskCategory": "MEDIUM",
  "primaryRiskReason": "Prior authorization is required but no prior authorization number is recorded.",
  "findings": [
    {
      "findingId": 1,
      "findingCode": "PRIOR_AUTH_MISSING",
      "description": "Prior authorization is required but no prior authorization number is recorded.",
      "points": 45
    }
  ],
  "aiSummary": "Administrative decision support only...",
  "recommendedActions": [
    "Verify prior authorization requirements and capture the authorization number before final disposition.",
    "Route to a human reviewer before any operational decision is finalized."
  ],
  "humanReviewRequired": true,
  "fallbackUsed": true,
  "createdAt": "2026-05-09T00:00:00Z"
}
```

## Public health endpoint

- `GET /api/health`

Example response:

```json
{
  "status": "UP",
  "application": "ClaimGuard AI Backend",
  "version": "0.6.0",
  "environment": "local"
}
```

## What is intentionally not implemented yet

- full PostgreSQL schema
- real AI provider calls
- production PHI handling
- dashboard or reporting endpoints
- document upload or OCR
- payer, EHR, clearinghouse, or other external healthcare integrations
- audit persistence implementation beyond structural foundations
- admin user management workflows
