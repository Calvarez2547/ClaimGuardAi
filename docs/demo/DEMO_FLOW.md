# Demo Flow

This guide walks through a recruiter-friendly backend demo of ClaimGuard AI using the current local profile behavior.

## Goal

Demonstrate that the backend can:

- start cleanly
- expose a public health endpoint
- authenticate a user
- enforce protected routes
- create and retrieve owner-scoped claims
- record review workflow notes
- run deterministic analysis with a structured risk breakdown
- optionally run OpenAI-backed reviewer summary generation
- aggregate data in a dashboard summary

## Prerequisites

- Java 21
- Maven 3.9+
- local terminal with `curl.exe`

## Start the backend

From `backend/`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Expected local demo credentials:

- username: `local.analyst`
- password: `LocalPass123!`

Default local AI behavior:

- `AI_ENABLED=false`
- analysis still works through deterministic fallback behavior

## 1. Verify health

```bash
curl.exe http://localhost:8080/api/health
```

What to call out:

- service is up
- version is visible
- environment is `local`

## 2. Show protected behavior without a token

```bash
curl.exe http://localhost:8080/api/auth/me
```

What this demonstrates:

- protected routes reject unauthenticated access
- the API returns a structured `401` response
- correlation IDs are included

## 3. Log in and copy the JWT

```powershell
curl.exe -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"local.analyst\",\"password\":\"LocalPass123!\"}"
```

Copy the `accessToken` field.

Optional PowerShell helper:

```powershell
$login = curl.exe -s -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"local.analyst\",\"password\":\"LocalPass123!\"}" | ConvertFrom-Json
$token = $login.accessToken
```

## 4. Confirm the authenticated identity

```powershell
curl.exe http://localhost:8080/api/auth/me `
  -H "Authorization: Bearer <jwt>"
```

What to call out:

- username, email, and role are returned
- this is the identity used for owner scoping

## 5. Create a clean claim

```powershell
curl.exe -X POST http://localhost:8080/api/claims `
  -H "Authorization: Bearer <jwt>" `
  -H "Content-Type: application/json" `
  -d "{\"claimNumber\":\"CLM-DEMO-1001\",\"patientControlNumber\":\"PCN-DEMO-1001\",\"payerName\":\"Acme Health Plan\",\"providerName\":\"North Valley Clinic\",\"serviceDate\":\"2026-05-01\",\"billedAmount\":1250.75,\"priorAuthRequired\":false,\"claimNotes\":\"Initial clean claim intake documentation with sufficient administrative detail.\"}"
```

Copy the returned claim `id`.

Optional PowerShell helper:

```powershell
$claim = curl.exe -s -X POST http://localhost:8080/api/claims `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d "{\"claimNumber\":\"CLM-DEMO-1001\",\"patientControlNumber\":\"PCN-DEMO-1001\",\"payerName\":\"Acme Health Plan\",\"providerName\":\"North Valley Clinic\",\"serviceDate\":\"2026-05-01\",\"billedAmount\":1250.75,\"priorAuthRequired\":false,\"claimNotes\":\"Initial clean claim intake documentation with sufficient administrative detail.\"}" | ConvertFrom-Json
$claimId = $claim.id
```

## 6. List claims

```powershell
curl.exe http://localhost:8080/api/claims `
  -H "Authorization: Bearer <jwt>"
```

What to call out:

- claim summaries are returned
- newest-first ordering
- owner-scoped data only

## 7. Retrieve claim details

```powershell
curl.exe http://localhost:8080/api/claims/<claimId> `
  -H "Authorization: Bearer <jwt>"
```

What to call out:

- default status is `RECEIVED`
- claim metadata is stored and returned consistently

## 8. Add and view review notes

Add a note:

```powershell
curl.exe -X POST http://localhost:8080/api/claims/<claimId>/review-notes `
  -H "Authorization: Bearer <jwt>" `
  -H "Content-Type: application/json" `
  -d "{\"noteText\":\"Reviewed claim details and confirmed it is ready for automated analysis.\"}"
```

List notes:

```powershell
curl.exe http://localhost:8080/api/claims/<claimId>/review-notes `
  -H "Authorization: Bearer <jwt>"
```

## 9. Run AI analysis

```powershell
curl.exe -X POST http://localhost:8080/api/claims/<claimId>/analyze `
  -H "Authorization: Bearer <jwt>"
```

What to call out:

- deterministic backend scoring is the source of truth
- by default local startup uses deterministic fallback summary behavior
- if OpenAI is enabled, `aiSummary` becomes provider-backed while the score, category, findings, and human-review flag remain backend-owned
- `fallbackUsed` makes the behavior explicit

## 10. Retrieve latest analysis

```powershell
curl.exe http://localhost:8080/api/claims/<claimId>/analysis/latest `
  -H "Authorization: Bearer <jwt>"
```

## 11. Retrieve analysis history

```powershell
curl.exe http://localhost:8080/api/claims/<claimId>/analysis/history `
  -H "Authorization: Bearer <jwt>"
```

What to call out:

- analyses are persisted
- history is available for repeated runs

## 12. View dashboard summary

```powershell
curl.exe http://localhost:8080/api/dashboard/summary `
  -H "Authorization: Bearer <jwt>"
```

What to call out:

- counts by status
- counts by risk category
- recent claims
- recent analyses
- high-risk claim summary
- top risk factors

## Optional: demonstrate a riskier claim

Create a second claim that intentionally triggers review:

```powershell
curl.exe -X POST http://localhost:8080/api/claims `
  -H "Authorization: Bearer <jwt>" `
  -H "Content-Type: application/json" `
  -d "{\"claimNumber\":\"CLM-DEMO-2001\",\"payerName\":\"Acme Health Plan\",\"providerName\":\"North Valley Clinic\",\"serviceDate\":\"2026-05-01\",\"billedAmount\":15000.00,\"priorAuthRequired\":true,\"claimNotes\":\"Too short\"}"
```

Then run `/analyze` on that claim and point out:

- higher score
- non-empty findings
- `humanReviewRequired: true`
- dashboard summary changes

## Optional: enable OpenAI locally

Restart the backend with:

```powershell
$env:AI_ENABLED="true"
$env:AI_PROVIDER="OPENAI"
$env:AI_API_KEY="replace-with-a-real-openai-api-key"
$env:AI_MODEL="gpt-4o-mini"
$env:AI_TIMEOUT_SECONDS="30"
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

What to call out:

- the endpoint contract does not change
- deterministic scoring still controls the analysis record
- `aiSummary` becomes provider-backed when the OpenAI call succeeds
- fallback remains available if the provider call fails

## Owner-scoping note

There is no public registration or admin user-management API in this scoped backend, so a live two-user demo is not the primary path. Owner scoping is already covered by integration tests:

- `ClaimIntakeIntegrationTest`
- `ClaimLifecycleReviewIntegrationTest`
- `ClaimAnalysisIntegrationTest`
- `DashboardIntegrationTest`

If you need a live two-user walkthrough, create a second user record through the local persistence layer or run against a file-backed local database and restart with a different seeded local user configuration.

## What this demo proves

- the project is backend-first and runnable
- auth and owner scoping are enforced
- the claim workflow is coherent end to end
- deterministic scoring is inspectable and remains authoritative
- AI output is reviewer support only
- the backend is polished enough for portfolio review without overstating real-world integration maturity
