# Version

- `v1.2.0`
- Phase: `Full-Stack Feature Completeness + Cloud Deployment`

## Version summary

ClaimGuard AI v1.2.0 closes all gaps between the portfolio documentation and the live codebase. New features shipped end-to-end (backend + frontend):

- **Tailwind CSS** — full design system migration; all components and pages converted
- **5-role RBAC** — `BILLING_SPECIALIST`, `REVENUE_CYCLE_ANALYST`, `CODING_REVIEWER`, `REVENUE_CYCLE_MANAGER`, `ADMINISTRATOR`; method-level `@PreAuthorize` on all admin/audit endpoints
- **User Registration** — `POST /api/auth/register` with username/email uniqueness enforcement; matching registration page
- **Token Refresh & Logout** — rotating UUID refresh tokens stored in DB; `POST /api/auth/refresh` and `POST /api/auth/logout`; frontend auto-retries on 401
- **Rate Limiting** — Bucket4j in-memory filter: 10 req/min per IP on login and register endpoints; 429 response with `Retry-After` header
- **Audit Logging** — `audit_events` table; async `AuditService` instruments auth, claims, and analysis flows; `GET /api/audit/events` (admin-only) and `GET /api/audit/events/me`; paginated `AuditLogPage`
- **Admin User Management** — `GET/PATCH /api/admin/users/**` endpoints (ADMINISTRATOR-only); `AdminUsersPage` with enable/disable toggle and role display
- **Cloud Deployment** — `render.yaml` for Render (Docker + PostgreSQL); Cloudflare Pages `_redirects`/`_headers`; `vite.config.ts` base configuration

## Database migrations

| Version | Purpose |
|---------|---------|
| V1–V7 | Existing (unchanged) |
| V8 | Role enum data migration |
| V9 | Refresh tokens table |
| V10 | Audit events table |
