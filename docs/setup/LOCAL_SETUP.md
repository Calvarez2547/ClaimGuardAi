# Local Setup

This project is designed to start locally with minimal setup.

## Prerequisites

- Java 21
- Maven 3.9+

## Default local behavior

- profile: `local`
- database: in-memory H2
- seeded user: enabled
- JWT secret: local development default
- port: `8080`

Default local credentials:

- username: `local.analyst`
- password: `LocalPass123!`

## Start locally

From `backend/`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Verify startup

```bash
curl.exe http://localhost:8080/api/health
```

Expected response fields:

- `status`
- `application`
- `version`
- `environment`

## Useful local overrides

PowerShell examples:

```powershell
$env:SERVER_PORT="8081"
$env:CLAIMGUARDAI_LOCAL_SEED_USER_USERNAME="demo.user"
$env:CLAIMGUARDAI_LOCAL_SEED_USER_EMAIL="demo.user@claimguardai.local"
$env:CLAIMGUARDAI_LOCAL_SEED_USER_PASSWORD="DemoPass123!"
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Available local overrides:

- `SERVER_PORT`
- `JWT_SECRET`
- `CLAIMGUARDAI_LOCAL_DB_URL`
- `CLAIMGUARDAI_LOCAL_DB_USERNAME`
- `CLAIMGUARDAI_LOCAL_DB_PASSWORD`
- `CLAIMGUARDAI_LOCAL_DB_DRIVER`
- `CLAIMGUARDAI_LOCAL_JPA_PLATFORM`
- `CLAIMGUARDAI_LOCAL_SEED_USER_ENABLED`
- `CLAIMGUARDAI_LOCAL_SEED_USER_USERNAME`
- `CLAIMGUARDAI_LOCAL_SEED_USER_EMAIL`
- `CLAIMGUARDAI_LOCAL_SEED_USER_PASSWORD`

## Notes

- Local startup does not require PostgreSQL.
- The seeded local user is for development and demonstration only.
- The current local demo path is backend-only.
