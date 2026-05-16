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
- AI provider: disabled by default
- port: `8080`

Default local credentials:

- username: `local.analyst`
- password: `LocalPass123!`

## Start locally

From `backend/`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Enable OpenAI locally

PowerShell example:

```powershell
$env:AI_ENABLED="true"
$env:AI_PROVIDER="OPENAI"
$env:AI_API_KEY="replace-with-a-real-openai-api-key"
$env:AI_MODEL="gpt-4o-mini"
$env:AI_TIMEOUT_SECONDS="30"
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Behavior:

- if `AI_ENABLED=false`, claim analysis uses deterministic fallback summaries
- if `AI_ENABLED=true` and config is valid, claim analysis calls OpenAI during `/api/claims/{claimId}/analyze`
- if `AI_ENABLED=true` and the OpenAI call fails, the backend falls back safely and returns `fallbackUsed=true`
- do not store real API keys in the repository

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
- `AI_ENABLED`
- `AI_PROVIDER`
- `AI_API_KEY`
- `AI_MODEL`
- `AI_TIMEOUT_SECONDS`
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
- This project is not production-ready for real PHI and does not claim HIPAA compliance.
- There is no real payer, EHR, or clearinghouse integration.
- The current local demo path is backend-only.
