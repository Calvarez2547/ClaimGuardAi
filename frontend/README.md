# ClaimGuard AI Frontend

React, TypeScript, and Vite frontend MVP for the ClaimGuard AI portfolio project.

## Scope

The frontend is a local prototype for authenticated healthcare revenue-cycle claim review. It calls the Spring Boot backend only:

```text
Frontend -> Spring Boot backend -> optional OpenAI provider
```

The frontend never calls OpenAI directly and must not contain API keys, JWT secrets, database passwords, or real PHI.

## Local URLs

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`

## Environment

Create or keep `frontend/.env` with:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Start the backend

From the repository root:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Default local credentials:

- username: `local.analyst`
- password: `LocalPass123!`

## Start the frontend

From the repository root:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## MVP Flow

1. Log in with the local demo credentials.
2. View the dashboard summary backed by `GET /api/dashboard/summary`.
3. Create a fake/demo claim.
4. List, search, and filter claims.
5. Open claim detail.
6. Update claim workflow status.
7. Add and view review notes.
8. Run or re-run backend-owned AI-assisted analysis.
9. View latest analysis, scoring details, risk factors, recommended actions, and history.
10. Log out.

## Backend Endpoints Used

- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/dashboard/summary`
- `POST /api/claims`
- `GET /api/claims`
- `GET /api/claims/{claimId}`
- `PATCH /api/claims/{claimId}/status`
- `POST /api/claims/{claimId}/review-notes`
- `GET /api/claims/{claimId}/review-notes`
- `POST /api/claims/{claimId}/analyze`
- `GET /api/claims/{claimId}/analysis/latest`
- `GET /api/claims/{claimId}/analysis/history`

## Safety Notes

- Use fake/demo claim data only.
- Do not enter real PHI.
- AI output is reviewer support only.
- Deterministic backend scoring remains the source of truth.
- This prototype does not claim HIPAA compliance or production readiness.

## Verification

```bash
npm run build
npm run lint
```
