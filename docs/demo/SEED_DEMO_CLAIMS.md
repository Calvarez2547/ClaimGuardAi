# Seed Demo Claims

Use `scripts/dev/seed-demo-claims.ps1` to create a local batch of fake/demo claims for frontend testing.

This script is for local development only. Do not use real PHI, real patient names, real payer data, real provider data, or production credentials.

## Start Backend

From the repository root:

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The local profile seeds the documented demo user:

- username: `local.analyst`
- password: `LocalPass123!`

## Seed 30 Claims

From the repository root:

```powershell
.\scripts\dev\seed-demo-claims.ps1
```

Equivalent explicit command:

```powershell
.\scripts\dev\seed-demo-claims.ps1 -ApiBaseUrl "http://localhost:8080" -Username "local.analyst" -Password "LocalPass123!" -ClaimCount 30
```

## Seed 50 Claims

```powershell
.\scripts\dev\seed-demo-claims.ps1 -ClaimCount 50
```

## Notes

- `ClaimCount` is limited to `20` through `50`.
- The script logs in through `POST /api/auth/login`.
- The script creates claims through `POST /api/claims`.
- Claim status is not sent during creation because the backend creation DTO does not support it; the backend assigns the initial status.
- Claim type is included only inside demo notes because the backend creation DTO does not support a `claimType` field.
- Some claims intentionally include high billed amounts, missing prior authorization numbers, short notes, or missing patient control numbers so backend analysis has useful scenarios to score later.

After seeding, start the frontend and open:

```text
http://localhost:5173
```
