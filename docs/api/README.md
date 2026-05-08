# API

All `/api/claims/**` endpoints require a valid Bearer token and are scoped to the authenticated user.

## Claim lifecycle and review endpoints

- `PATCH /api/claims/{claimId}/status`
- `POST /api/claims/{claimId}/review-notes`
- `GET /api/claims/{claimId}/review-notes`

Status update request:

```json
{
  "status": "IN_REVIEW"
}
```

Review note create request:

```json
{
  "noteText": "Reviewed claim details and flagged missing provider information."
}
```

Review note response:

```json
{
  "id": 1,
  "claimId": 10,
  "noteText": "Reviewed claim details and flagged missing provider information.",
  "createdAt": "2026-05-08T12:00:00Z",
  "updatedAt": "2026-05-08T12:00:00Z"
}
```

Requests for claims owned by another user return the same structured 404 response as missing claims.
