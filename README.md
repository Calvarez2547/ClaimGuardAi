# ClaimGuardAi
Professional README Document
AI-Assisted Revenue Cycle Denial Prevention Platform


Prepared by	Christian Alvarez
Document Type	Professional Project README
Project Type	Healthcare Revenue Cycle / AI-Assisted Full-Stack Web Application
Primary Stack	Java Spring Boot, PostgreSQL, React, TypeScript, AI Agent Service
Audience	Recruiters, hiring managers, software engineers, AI/data engineering reviewers
Version	1.0

Portfolio Disclaimer
ClaimGuard AI uses mock and synthetic healthcare claim data only. It is not intended for production medical, billing, coding, payer, legal, or compliance decision-making. No real protected health information should be entered into this application.
 
1. Executive Project Summary
ClaimGuard AI is a full-stack healthcare revenue cycle application that uses a Java Spring Boot backend, PostgreSQL database, rule-based validation, and an AI-assisted claim review agent to identify potential insurance claim denial risks before submission.
The project is designed as a professional software engineering portfolio application that demonstrates backend development, relational database design, REST API architecture, AI-agent workflow design, security-aware engineering, and business-focused healthcare technology.
The application simulates a pre-submission review workflow where a revenue cycle user can submit a mock claim, receive a denial risk score, review AI-generated recommendations, and route the claim to the appropriate work queue for human review.
2. Business Problem
Hospitals and healthcare organizations can lose time and revenue when claims are delayed, rejected, or denied because of preventable issues. ClaimGuard AI addresses this problem by identifying risk indicators earlier in the claim workflow and presenting corrective guidance in business-readable language.
•	Missing prior authorization information.
•	Incomplete supporting documentation.
•	Invalid or incomplete claim data.
•	Potential duplicate claim indicators.
•	Insufficient coding or documentation support.
•	Eligibility or payer verification concerns.
ClaimGuard AI is intentionally positioned as an AI-assisted decision-support layer. It does not replace billing professionals, certified coders, payer systems, clinicians, compliance teams, or legal review.
3. Key Capabilities
•	Secure user authentication and role-based access control.
•	Claim intake form for mock healthcare claims.
•	Rule-based validation engine for denial risk indicators.
•	AI-assisted claim review agent.
•	Risk scoring from 0 to 100.
•	Low, Medium, and High risk classification.
•	AI-generated denial risk summary.
•	Corrective action checklist.
•	Recommended work queue routing.
•	Claim workflow statuses.
•	Audit logging for claim and AI activity.
•	Revenue cycle analytics dashboard.
•	Searchable and filterable claims table.
•	Admin configuration for users and validation rules.
•	Security controls for SQL injection, XSS, IDOR/BOLA, CORS, secrets, and prompt injection risks.
4. AI Agent Workflow
The AI agent is not implemented as a generic chatbot. It is integrated into a structured backend workflow that combines deterministic business rules with AI-generated explanation and recommendation support.
User submits mock claim
        ↓
Spring Boot API validates request
        ↓
Claim is stored in PostgreSQL
        ↓
Rules engine checks denial risk indicators
        ↓
Risk scoring service calculates preliminary risk score
        ↓
AI agent receives structured claim data + rule findings
        ↓
AI agent generates plain-language explanation and recommendations
        ↓
Routing service recommends work queue
        ↓
Results are saved with audit logs
        ↓
Frontend displays AI claim review results

The AI agent helps generate the following output artifacts:
•	Executive claim risk summary.
•	Denial risk explanation.
•	Corrective action checklist.
•	Recommended review queue explanation.
•	Human review warning.
•	Business-readable explanation of rule findings.
The final workflow remains human-in-the-loop. The AI does not submit claims, approve claims, make final payer determinations, or replace professional review.
5. Example AI Claim Review Output
Risk Level: High
Risk Score: 86/100
Primary Issue: Missing prior authorization
Recommended Queue: Authorization Team
Human Review Required: Yes

AI-Generated Summary:
This claim appears to have a high denial risk because the selected service is marked as requiring prior authorization, but no authorization number is attached to the claim. Documentation notes are also limited and may not sufficiently support pre-submission review.

Recommended Corrective Actions:
1. Confirm authorization requirement with the payer.
2. Obtain and attach the prior authorization number.
3. Expand supporting documentation notes.
4. Route the claim to the Authorization Team.
5. Reanalyze the claim after corrections.

6. Technology Stack
Layer	Technology
Frontend	React, TypeScript, Tailwind CSS
Backend	Java, Spring Boot
API Style	REST API
Database	PostgreSQL
ORM / Data Access	Spring Data JPA / Hibernate
Security	Spring Security, JWT, BCrypt, RBAC
AI Integration	AI provider API through backend service layer
Testing	JUnit, Mockito, Spring Boot Test, Postman/Newman
Deployment	Cloudflare Pages frontend, Render/Railway/AWS backend, PostgreSQL cloud database
Version Control	Git and GitHub
7. Main User Roles
Role	Description
Billing Specialist	Creates claims, submits claims for review, updates claim status.
Revenue Cycle Analyst	Reviews risk findings, manages claim corrections, routes claims.
Coding Reviewer	Reviews coding-related issues and documentation support.
Revenue Cycle Manager	Views dashboards, reports, trends, and high-risk claim metrics.
Administrator	Manages users, roles, validation rules, and system settings.
8. Core Application Pages
Dashboard
•	Total claims reviewed
•	High-risk claims
•	Missing prior authorization count
•	Claim amount at risk
•	Risk distribution
•	Top denial risk reasons
•	Recent AI analysis activity
Claims List
•	Risk category filter
•	Claim status filter
•	Payer filter
•	Department filter
•	Date range filter
•	Denial risk reason search
New Claim
•	Patient identifier
•	Payer
•	Diagnosis code
•	Procedure code
•	Claim amount
•	Date of service
•	Prior authorization status
•	Documentation notes
AI Claim Review Results
•	Risk score
•	Risk category
•	Primary issue
•	AI-generated summary
•	Rule-based findings
•	Corrective actions
•	Recommended queue
•	Audit trail
Work Queues
•	Authorization Team
•	Coding Review
•	Billing Review
•	Insurance Verification
•	Clinical Documentation Review
•	Manager Review
Administration
•	Users
•	Roles
•	Validation rules
•	Rule weights
•	Security settings
9. Security Design Highlights
SQL Injection Protection
•	Uses Spring Data JPA repositories and parameterized queries.
•	Avoids string-concatenated SQL.
•	Validates request DTOs before persistence.
•	Uses database constraints for data integrity.
Authentication and Authorization
•	JWT-based authentication.
•	BCrypt password hashing.
•	Role-based access control.
•	Protected REST endpoints.
•	Method-level authorization with Spring Security.
IDOR / BOLA Protection
•	Backend verifies whether the authenticated user is allowed to access a requested claim.
•	Admin endpoints are restricted to administrator users.
•	Frontend hiding is not treated as a security boundary.
XSS Protection
•	React renders user-entered notes as escaped text.
•	Application avoids unsafe HTML rendering.
•	AI output is displayed as structured text, not raw HTML.
CORS and CSRF Controls
•	CORS restricted to approved frontend origins.
•	Cookie-based session deployments should enable CSRF protection.
•	JWT deployments should use Authorization headers and HTTPS.
AI Prompt Injection Protection
•	User-entered claim notes are treated as untrusted data.
•	Backend prompt templates instruct the model not to follow instructions inside claim fields.
•	Risk score and routing are validated by deterministic backend services.
•	AI output is validated against an expected structured schema.
10. Secrets Management
The following values must never be committed to GitHub. They should be stored as local environment variables or deployment platform secrets.
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
AI_API_KEY

11. Project Architecture
claimguard-ai/
├── backend/
│   ├── src/main/java/com/claimguard/
│   │   ├── auth/
│   │   ├── claims/
│   │   ├── analysis/
│   │   ├── ai/
│   │   ├── rules/
│   │   ├── scoring/
│   │   ├── routing/
│   │   ├── dashboard/
│   │   ├── audit/
│   │   ├── admin/
│   │   ├── security/
│   │   └── common/
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── hooks/
│   │   ├── types/
│   │   ├── utils/
│   │   └── App.tsx
│   └── package.json
│
├── docs/
│   ├── SRS/
│   ├── SDS/
│   ├── API_SPEC.md
│   ├── DATABASE_DESIGN.md
│   ├── AI_AGENT_DESIGN.md
│   ├── SECURITY_THREAT_MODEL.md
│   └── DEPLOYMENT_GUIDE.md
│
└── README.md

12. Backend Modules
Module	Responsibility
Auth Module	Login, JWT generation, password validation, user identity.
Claims Module	Claim creation, retrieval, update, filtering, and status management.
Rules Module	Deterministic denial risk validation rules.
Scoring Module	Risk score calculation and risk category assignment.
AI Module	AI prompt construction, AI API call, response validation, fallback handling.
Routing Module	Work queue recommendation logic.
Dashboard Module	Aggregated metrics and trend data.
Audit Module	Security, claim, and AI activity logging.
Admin Module	User, role, and rule configuration.
Security Module	Spring Security configuration, RBAC, CORS, authorization rules.
13. Main API Endpoints
Authentication
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me

Claims
GET    /api/claims
POST   /api/claims
GET    /api/claims/{claimId}
PUT    /api/claims/{claimId}
PATCH  /api/claims/{claimId}/status

AI Analysis
POST /api/claims/{claimId}/analyze
GET  /api/claims/{claimId}/analysis/latest
GET  /api/claims/{claimId}/analysis/history

Dashboard
GET /api/dashboard/summary
GET /api/dashboard/risk-distribution
GET /api/dashboard/top-denial-reasons
GET /api/dashboard/recent-activity

Work Queues
GET /api/work-queues
GET /api/work-queues/{queueName}/claims

Audit
GET /api/audit/claims/{claimId}
GET /api/audit/security-events

Administration
GET    /api/admin/users
POST   /api/admin/users
PATCH  /api/admin/users/{userId}/roles
GET    /api/admin/rules
PATCH  /api/admin/rules/{ruleId}

14. Sample Claim Request
{
  "patientIdentifier": "PT-48392",
  "payerName": "Aetna",
  "memberId": "AET-9048821",
  "providerName": "Dr. Maria Chen",
  "providerDepartment": "Radiology",
  "dateOfService": "2025-06-07",
  "diagnosisCodes": ["G44.1"],
  "procedureCodes": ["70551"],
  "claimAmount": 2850.00,
  "priorAuthRequired": true,
  "priorAuthNumber": null,
  "documentationNotes": "Patient reports recurring headaches. MRI ordered for further review.",
  "internalNotes": "Mock claim for demo purposes."
}

15. Sample AI Analysis Response
{
  "claimId": "2025-CLM-01987",
  "riskScore": 86,
  "riskCategory": "HIGH",
  "primaryRiskReason": "Missing prior authorization",
  "recommendedQueue": "AUTHORIZATION_TEAM",
  "humanReviewRequired": true,
  "aiSummary": "This claim appears to have a high denial risk because the selected service is marked as requiring prior authorization, but no authorization number is attached.",
  "riskFactors": [
    "Prior authorization required but not provided",
    "Documentation notes are incomplete",
    "Claim should not proceed to submission in current state"
  ],
  "recommendedActions": [
    "Confirm authorization requirement with the payer",
    "Obtain and attach prior authorization number",
    "Expand supporting documentation notes",
    "Route claim to Authorization Team",
    "Reanalyze claim after corrections"
  ],
  "limitationStatement": "This analysis is for administrative decision support only and does not represent a final payer, coding, legal, or clinical determination."
}

16. Local Development Setup
Prerequisites
•	Java 21 or Java 17
•	Maven
•	Node.js
•	PostgreSQL
•	Git
•	IntelliJ IDEA or VS Code
•	Postman or Insomnia for API testing
Backend Setup
cd backend

DATABASE_URL=jdbc:postgresql://localhost:5432/claimguard_ai
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password
JWT_SECRET=your_secure_jwt_secret
AI_API_KEY=your_ai_provider_key

mvn spring-boot:run

Frontend Setup
cd frontend
npm install
npm run dev

Database Setup
CREATE DATABASE claimguard_ai;

Recommended migration path:
backend/src/main/resources/db/migration/V1__initial_schema.sql

17. Environment Variables
Variable	Description
DATABASE_URL	PostgreSQL JDBC connection string.
DATABASE_USERNAME	Database username.
DATABASE_PASSWORD	Database password.
JWT_SECRET	Secret key used to sign JWT tokens.
AI_API_KEY	API key for AI provider.
CORS_ALLOWED_ORIGINS	Approved frontend origins.
APP_ENV	local, staging, or production.
18. Testing Strategy
Test Type	Purpose
Unit Tests	Test rules, scoring, routing, and utility logic.
Integration Tests	Test API, database, and service interactions.
Security Tests	Test authentication, authorization, input validation, and access control.
AI Contract Tests	Test structured AI response handling and fallback behavior.
Frontend Tests	Test forms, dashboard components, and error states.
API Tests	Validate endpoint request and response behavior.
mvn test

19. Development Roadmap
Phase 1: Project Foundation
•	Initialize GitHub repository
•	Create backend Spring Boot project
•	Create frontend React project
•	Configure PostgreSQL
•	Add documentation folder
Phase 2: Core Backend
•	Create database schema
•	Build claim entity and repository
•	Build claim REST API
•	Add DTO validation
•	Add audit logging foundation
Phase 3: Security Foundation
•	Add Spring Security
•	Add JWT login
•	Add BCrypt password hashing
•	Add role-based access control
•	Add CORS configuration
•	Protect API endpoints
Phase 4: Rule-Based Risk Engine
•	Implement validation rules
•	Implement severity levels
•	Implement risk score calculation
•	Store validation findings
•	Add unit tests
Phase 5: AI Agent Integration
•	Build AI prompt template
•	Send structured claim data and rule findings to AI provider
•	Validate AI response schema
•	Add fallback behavior
•	Store AI results and metadata
Phase 6: Frontend Dashboard
•	Build login page
•	Build dashboard cards
•	Build claims table
•	Build claim intake form
•	Build AI results page
•	Build work queue view
Phase 7: Reports and Admin
•	Add CSV export
•	Add admin user management
•	Add rule management
•	Add audit log view
Phase 8: Deployment and Polish
•	Add seeded demo data
•	Deploy frontend
•	Deploy backend
•	Configure cloud database
•	Add screenshots and demo video
•	Finalize documentation
20. Portfolio Value
•	Java backend development
•	Spring Boot REST API design
•	PostgreSQL relational database modeling
•	Secure authentication and authorization
•	Healthcare revenue cycle workflow understanding
•	AI-agent orchestration
•	Prompt injection defense design
•	Rule-based decision engine design
•	Data validation and audit logging
•	Full-stack dashboard development
•	Cloud-ready deployment planning
•	Professional documentation with SRS and SDS artifacts
21. Suggested Resume Bullet
Built ClaimGuard AI, a full-stack healthcare revenue cycle platform using Java, Spring Boot, PostgreSQL, React, and an AI-assisted claim review agent to detect mock insurance claim denial risks, generate corrective recommendations, route claims to business work queues, and maintain audit logs with role-based security controls.
22. Project Status
Current status: Planning and design phase.
Completed Documentation
•	Software Requirements Specification
•	Software Design Specification
•	Security-enhanced SRS Version 1.2
•	Security-enhanced SDS Version 1.2
•	README
•	API Specification
•	Database Design Document
•	AI Agent Design Document
•	Security Threat Model
Planned Next Documents
•	Test Plan
•	Deployment Guide
•	User Guide
•	Architecture Decision Records
23. Author
Christian Alvarez
Software Engineering Student | Healthcare IT Specialist | Backend and AI Engineering Portfolio Builder
Portfolio: christianalvarezdev.pages.dev
24. Important Disclaimer
ClaimGuard AI is a software engineering portfolio project. It is not a certified billing system, medical device, clinical decision support system, payer adjudication tool, or compliance platform. The application uses mock healthcare claim data only and should not be used with real protected health information.
