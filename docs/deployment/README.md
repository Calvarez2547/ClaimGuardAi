# Deployment

This project includes a production-style deployment posture for a backend portfolio project. It is intentionally lightweight: one Spring Boot service, Flyway migrations, environment-based configuration, PostgreSQL-oriented production settings, and a Dockerfile. It does not include Kubernetes, Terraform, or a full CI/CD stack in this phase.

## Current deployment stance

- local and test profiles use H2
- production profile expects PostgreSQL
- Flyway runs database migrations at startup
- production startup validates critical security configuration
- container build support exists in `backend/Dockerfile`

## Production requirements

Required environment variables:

- `SPRING_PROFILES_ACTIVE=prod`
- `JWT_SECRET`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Recommended:

- `CORS_ALLOWED_ORIGINS`
- `JWT_EXPIRATION_MINUTES`
- `SERVER_PORT`

## Production safeguards

Startup is expected to fail when:

- `JWT_SECRET` is missing
- `JWT_SECRET` is shorter than 32 characters
- a known local or test placeholder JWT secret is used
- wildcard CORS origins are configured
- the local seed user is enabled

## Local production-style run

From `backend/`:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:JWT_SECRET="replace-with-a-strong-secret-at-least-32-characters"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/claimguardai"
$env:SPRING_DATASOURCE_USERNAME="claimguardai"
$env:SPRING_DATASOURCE_PASSWORD="change-me"
mvn spring-boot:run
```

## Container workflow

Build:

```bash
docker build -t claimguardai-backend:1.0.0-rc backend
```

Run:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SECRET=replace-with-a-strong-secret-at-least-32-characters \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/claimguardai \
  -e SPRING_DATASOURCE_USERNAME=claimguardai \
  -e SPRING_DATASOURCE_PASSWORD=change-me \
  -e CORS_ALLOWED_ORIGINS=http://localhost:5173 \
  claimguardai-backend:1.0.0-rc
```

## Release-readiness notes

- This repo is deployment-ready for demo and portfolio review.
- It is not presented as a fully operated production environment.
- Secrets management, observability, backup strategy, and infrastructure automation would be the next layer of work for a real deployment program.
