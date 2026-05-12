# Security

ClaimGuard AI uses a production-style security baseline that fits a backend portfolio project. The emphasis is on access control, predictable API behavior, defensive defaults, and honest boundaries.

## Implemented controls

- JWT Bearer authentication for protected endpoints
- deny-by-default API security configuration
- owner scoping for claim-bound resources
- password hashing for stored local users
- structured auth and validation error responses
- request correlation IDs through success and failure paths
- hardened response headers
- explicit CORS configuration with production guardrails
- startup validation for production profile secrets and seed-user safety

## Protected resources

All endpoints except `GET /api/health` and `POST /api/auth/login` require authentication.

Owner scoping is enforced for:

- claim detail retrieval
- claim status updates
- review notes
- analyses
- dashboard summaries

The user-visible behavior is deliberate: when a user requests another user’s claim-backed resource, the API returns `404 Claim not found.`

## Security boundaries

- no real PHI integrations
- no claim submission to external payers
- no EHR or clearinghouse connectivity
- no admin or superuser workflow surface in this phase
- no claim of certified HIPAA compliance

## Production notes

The production profile should be treated as a secure starting point, not a finished platform. Real deployment work would still need managed secrets, audit logging strategy, monitoring, incident response, and operational controls around infrastructure and data handling.
