# ClaimGuard AI

ClaimGuard AI is a healthcare revenue cycle portfolio project focused on improving claim quality before submission. The application will combine rule-based validation, AI-assisted claim review, auditability, and role-based workflows in a professional full-stack architecture.

## Planned stack

- Backend: Java Spring Boot
- Frontend: React with TypeScript
- Database: PostgreSQL
- Security: JWT authentication and role-based access control
- Quality controls: claim validation rules, audit logging, and test automation

## Current status

`v0.2.0 - Backend Foundation` is complete on the `feature/backend-foundation` branch.

The backend now includes a Spring Boot service built with Maven and Java 21, local and test H2 support, a Flyway baseline migration, a security baseline, a health endpoint, structured error handling, correlation ID support, and starter automated tests.

## Repository layout

- `backend/` reserved for the Spring Boot service
- `frontend/` reserved for the React application
- `docs/` project documentation organized by domain
- `database/` SQL migrations and seed data
- `scripts/` helper scripts for development and operations

## Initial development approach

The repository started with a monorepo and documentation foundation, followed by a backend infrastructure milestone designed to establish platform conventions before domain feature development begins.

