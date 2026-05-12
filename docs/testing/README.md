# Testing

The backend includes focused automated tests around the project’s highest-value behavior: authentication, owner scoping, validation, deterministic risk scoring, API contract stability, and production configuration safety.

## How to run

From `backend/`:

```bash
mvn test
```

## Current test coverage areas

- `AuthenticationIntegrationTest`
  - login success and failure paths
  - protected endpoint authentication requirements
  - current-user identity response
- `ClaimIntakeIntegrationTest`
  - claim creation
  - claim list/detail retrieval
  - validation behavior
  - owner scoping for claims
- `ClaimLifecycleReviewIntegrationTest`
  - claim status updates
  - review note creation and retrieval
  - owner scoping for lifecycle actions
- `ClaimAnalysisIntegrationTest`
  - analysis creation
  - latest/history retrieval
  - persisted findings
  - fallback summary behavior
  - structured risk breakdowns
- `DashboardIntegrationTest`
  - owner-scoped summary metrics
  - empty-state behavior
- `HealthControllerIntegrationTest`
  - public health endpoint
  - hardened headers
  - CORS preflight behavior
- `ProductionConfigurationValidatorTest`
  - required production secret validation
  - explicit CORS requirements
- `RiskScoringServiceTest`
  - deterministic scoring
  - score thresholds
  - human review routing conditions

## Testing posture

- integration tests run with the `test` profile
- H2 is used for repeatable local test execution
- the test profile seeds one local user for auth flows
- security and error behavior are treated as part of the contract

## Remaining gaps

- no performance or load testing
- no contract publishing or generated OpenAPI spec
- no end-to-end frontend tests because frontend work is out of scope for this phase
