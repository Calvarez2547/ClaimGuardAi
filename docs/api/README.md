# API Guide

ClaimGuard AI exposes a small backend-only REST API for authenticated claim review workflows. All examples below reflect the current codebase at `v1.1.0`.

## Base URL

- Local default: `http://localhost:8080`

## Authentication model

- Public endpoints: `GET /api/health`, `POST /api/auth/login`
- All other documented endpoints require `Authorization: Bearer <jwt>`
- Protected resources are owner-scoped
- Accessing another user's claim-backed resource returns `404 Claim not found.`
- Error responses include `X-Correlation-Id`

## Seeded local credentials

When the `local` profile runs with the default settings, one demo user is seeded automatically:

- username: `local.analyst`
- password: `LocalPass123!`

## Public endpoints

### `GET /api/health`

Returns service availability and runtime metadata.

Example:

```bash
curl.exe http://localhost:8080/api/health
```

Example response:

```json
{
  "status": "UP",
  "application": "ClaimGuard AI Backend",
  "version": "1.1.0",
  "environment": "local"
}
```

### `POST /api/auth/login`

Authenticates a user and returns a JWT.

Request body:

```json
{
  "username": "local.analyst",
  "password": "LocalPass123!"
}
```

Example:

```powershell
curl.exe -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"username\":\"local.analyst\",\"password\":\"LocalPass123!\"}"
```

Example response:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresAt": "2026-05-11T18:30:00Z"
}
```

## Auth endpoint

### `GET /api/auth/me`

Returns the authenticated identity.

Example:

```powershell
curl.exe http://localhost:8080/api/auth/me `
  -H "Authorization: Bearer <jwt>"
```

Example response:

```json
{
  "id": 1,
  "username": "local.analyst",
  "email": "local.analyst@claimguardai.local",
  "roles": ["USER"]
}
```

## Claim endpoints

### `POST /api/claims`

Creates a claim for the authenticated user.

Request body:

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
  "claimNotes": "Initial clean claim intake documentation with sufficient administrative detail."
}
```

Field rules:

- `claimNumber` required, max `80`
- `patientControlNumber` optional, max `80`
- `payerName` required, max `255`
- `providerName` required, max `255`
- `serviceDate` required, cannot be in the future
- `billedAmount` required, greater than `0`, max `10` integer digits and `2` decimals
- `priorAuthRequired` optional boolean
- `priorAuthNumber` optional, max `80`
- `claimNotes` optional, max `2000`

Example:

```powershell
curl.exe -X POST http://localhost:8080/api/claims `
  -H "Authorization: Bearer <jwt>" `
  -H "Content-Type: application/json" `
  -d "{\"claimNumber\":\"CLM-10001\",\"patientControlNumber\":\"PCN-1001\",\"payerName\":\"Acme Health Plan\",\"providerName\":\"North Valley Clinic\",\"serviceDate\":\"2026-05-01\",\"billedAmount\":1250.75,\"priorAuthRequired\":false,\"claimNotes\":\"Initial clean claim intake documentation with sufficient administrative detail.\"}"
```

Example response:

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
  "claimNotes": "Initial clean claim intake documentation with sufficient administrative detail.",
  "createdByUserId": 1,
  "createdAt": "2026-05-11T18:32:00Z",
  "updatedAt": "2026-05-11T18:32:00Z"
}
```

### `GET /api/claims`

Returns owner-scoped claim summaries ordered newest first.

Example:

```powershell
curl.exe http://localhost:8080/api/claims `
  -H "Authorization: Bearer <jwt>"
```

Example response:

```json
[
  {
    "id": 1,
    "claimNumber": "CLM-10001",
    "patientControlNumber": "PCN-1001",
    "payerName": "Acme Health Plan",
    "providerName": "North Valley Clinic",
    "serviceDate": "2026-05-01",
    "billedAmount": 1250.75,
    "claimStatus": "RECEIVED",
    "createdAt": "2026-05-11T18:32:00Z",
    "updatedAt": "2026-05-11T18:32:00Z"
  }
]
```

### `GET /api/claims/{claimId}`

Returns the full owner-scoped claim record.

Example:

```powershell
curl.exe http://localhost:8080/api/claims/1 `
  -H "Authorization: Bearer <jwt>"
```

### `PATCH /api/claims/{claimId}/status`

Updates claim workflow status for the owning user.

Allowed values:

- `RECEIVED`
- `DRAFT`
- `SUBMITTED`
- `IN_REVIEW`
- `NEEDS_INFO`
- `APPROVED`
- `DENIED`
- `CLOSED`

Request body:

```json
{
  "status": "IN_REVIEW"
}
```

Example:

```powershell
curl.exe -X PATCH http://localhost:8080/api/claims/1/status `
  -H "Authorization: Bearer <jwt>" `
  -H "Content-Type: application/json" `
  -d "{\"status\":\"IN_REVIEW\"}"
```

## Review note endpoints

### `POST /api/claims/{claimId}/review-notes`

Adds a review note to an owner-scoped claim.

Request body:

```json
{
  "noteText": "Reviewed claim details and flagged missing provider information."
}
```

Example response:

```json
{
  "id": 1,
  "claimId": 1,
  "noteText": "Reviewed claim details and flagged missing provider information.",
  "createdAt": "2026-05-11T18:35:00Z",
  "updatedAt": "2026-05-11T18:35:00Z"
}
```

### `GET /api/claims/{claimId}/review-notes`

Returns review notes for the owner-scoped claim.

Example:

```powershell
curl.exe http://localhost:8080/api/claims/1/review-notes `
  -H "Authorization: Bearer <jwt>"
```

## AI analysis endpoints

Deterministic backend scoring remains the source of truth. When AI is enabled and configured, the backend calls OpenAI to generate reviewer-support narrative output. When AI is disabled or a provider call fails, the backend falls back safely to deterministic summary generation and exposes that behavior through `fallbackUsed`.

AI runtime variables:

- `AI_ENABLED`
- `AI_PROVIDER`
- `AI_API_KEY`
- `AI_MODEL`
- `AI_TIMEOUT_SECONDS`

### `POST /api/claims/{claimId}/analyze`

Runs deterministic analysis and persists an analysis record.

Example:

```powershell
curl.exe -X POST http://localhost:8080/api/claims/1/analyze `
  -H "Authorization: Bearer <jwt>"
```

Example response:

```json
{
  "analysisId": 10,
  "claimId": 1,
  "riskScore": 45,
  "riskCategory": "MEDIUM",
  "primaryRiskReason": "Prior authorization is required but no prior authorization number is recorded.",
  "secondaryRiskReasons": [],
  "findings": [
    {
      "findingId": 22,
      "findingCode": "PRIOR_AUTH_MISSING",
      "description": "Prior authorization is required but no prior authorization number is recorded.",
      "points": 45,
      "category": "PRIOR_AUTHORIZATION",
      "label": "Missing prior authorization",
      "severity": "HIGH",
      "weight": 45,
      "triggered": true,
      "contribution": 45,
      "recommendedAction": "Verify whether prior authorization is required and attach or enter the authorization number before submission."
    }
  ],
  "scoreBreakdown": {
    "baseScore": 0,
    "totalScore": 45,
    "cappedScore": 45,
    "riskCategory": "MEDIUM",
    "primaryRiskReason": "Prior authorization is required but no prior authorization number is recorded.",
    "secondaryRiskReasons": [],
    "humanReviewRequired": true,
    "factors": [
      {
        "code": "PRIOR_AUTH_MISSING",
        "category": "PRIOR_AUTHORIZATION",
        "label": "Missing prior authorization",
        "description": "Prior authorization is required but no prior authorization number is recorded.",
        "severity": "HIGH",
        "weight": 45,
        "triggered": true,
        "contribution": 45,
        "recommendedAction": "Verify whether prior authorization is required and attach or enter the authorization number before submission."
      }
    ],
    "recommendedActions": [
      "Verify whether prior authorization is required and attach or enter the authorization number before submission.",
      "Route to a human reviewer before any operational decision is finalized."
    ]
  },
  "aiSummary": "AI-assisted reviewer support only. The claim should be reviewed for missing prior authorization details.\n\nRisk explanation: The deterministic score is elevated because prior authorization is required but not recorded.\n\nDocumentation concerns:\n- Prior authorization details are missing from the claim record.\n\nSuggested reviewer actions:\n- Confirm the prior authorization number with the ordering workflow.\n\nReview priority: HIGH\n\nDisclaimer: This is AI-assisted review support and not a final payer decision.",
  "recommendedActions": [
    "Verify whether prior authorization is required and attach or enter the authorization number before submission.",
    "Route to a human reviewer before any operational decision is finalized."
  ],
  "humanReviewRequired": true,
  "fallbackUsed": false,
  "createdAt": "2026-05-11T18:36:00Z"
}
```

Behavior notes:

- deterministic `riskScore`, `riskCategory`, `findings`, `scoreBreakdown`, and `humanReviewRequired` stay backend-owned
- `aiSummary` can be provider-backed or fallback-backed
- `recommendedActions` in the API response remain deterministic backend actions
- `fallbackUsed=true` means the provider was disabled or fallback logic was used after a provider failure

### `GET /api/claims/{claimId}/analysis/latest`

Returns the most recent analysis for the claim owner.

### `GET /api/claims/{claimId}/analysis/history`

Returns persisted analysis history ordered newest first.

## Risk scoring behavior

The exposed scoring behavior is deterministic:

- base score: `0`
- maximum score: `100`
- `LOW`: `0-39`
- `MEDIUM`: `40-69`
- `HIGH`: `70-100`

Current rule triggers:

- `PRIOR_AUTH_MISSING`: `45`
- `WEAK_DOCUMENTATION_NOTES`: `20`
- `MISSING_PATIENT_CONTROL_NUMBER`: `10`
- `HIGH_BILLED_AMOUNT`: `30`

Human review is required when:

- the final risk category is `HIGH`, or
- a medium-risk claim includes a high-impact review factor such as missing prior authorization or high billed amount, or
- a critical rule factor is present

## Dashboard summary endpoint

### `GET /api/dashboard/summary`

Returns owner-scoped summary metrics across claims and analyses.

Example:

```powershell
curl.exe http://localhost:8080/api/dashboard/summary `
  -H "Authorization: Bearer <jwt>"
```

Example response shape:

```json
{
  "totalClaims": 3,
  "claimsByStatus": [
    { "status": "SUBMITTED", "count": 1 },
    { "status": "APPROVED", "count": 1 },
    { "status": "DENIED", "count": 1 }
  ],
  "analysesByRiskCategory": [
    { "riskCategory": "LOW", "count": 1 },
    { "riskCategory": "MEDIUM", "count": 1 }
  ],
  "humanReviewRequiredCount": 1,
  "lowRiskCount": 1,
  "mediumRiskCount": 1,
  "highRiskCount": 0,
  "recentClaims": [],
  "recentAnalyses": [],
  "highestRiskClaims": [],
  "topRiskFactors": [],
  "generatedAt": "2026-05-11T18:40:00Z"
}
```

## Expected error responses

Structured error shape:

```json
{
  "timestamp": "2026-05-11T18:41:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication is required to access this resource.",
  "path": "/api/auth/me",
  "correlationId": "3d876366f1ec4dd5",
  "details": []
}
```

Common cases:

- `401 Unauthorized`: missing or invalid token
- `404 Not Found`: claim does not exist or does not belong to the authenticated user
- `400 Bad Request`: validation or malformed JSON

Example validation error:

```json
{
  "timestamp": "2026-05-11T18:42:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for request body.",
  "path": "/api/claims",
  "correlationId": "3115a6f706df4e13",
  "details": [
    {
      "field": "serviceDate",
      "message": "Service date cannot be in the future."
    }
  ]
}
```

Example malformed enum error:

```json
{
  "timestamp": "2026-05-11T18:42:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Malformed JSON request body.",
  "path": "/api/claims/1/status",
  "correlationId": "cbe912bf73d84952",
  "details": [
    {
      "field": "status",
      "message": "Value must match the expected type or enum value."
    }
  ]
}
```

## Suggested demo command sequence

- `GET /api/health`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/claims`
- `GET /api/claims`
- `GET /api/claims/{claimId}`
- `POST /api/claims/{claimId}/review-notes`
- `GET /api/claims/{claimId}/review-notes`
- `POST /api/claims/{claimId}/analyze`
- `GET /api/claims/{claimId}/analysis/latest`
- `GET /api/claims/{claimId}/analysis/history`
- `GET /api/dashboard/summary`

The full walkthrough is in [docs/demo/DEMO_FLOW.md](../demo/DEMO_FLOW.md).
