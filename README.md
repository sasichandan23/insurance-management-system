# Insurance Management System

A full-stack **Insurance Management System** built with **Spring Boot, React, and MySQL**, supporting all major insurance types — Life, Health, Motor, and Home — with JWT-secured, role-based portals for **Admin**, **Agent**, and **Customer**.

## Features

- **Authentication & Security** — JWT-based stateless authentication with Spring Security, BCrypt password hashing, and role-based access control (Admin / Agent / Customer)
- **Insurance Plan Catalogue** — Admin manages plans across LIFE, HEALTH, MOTOR, and HOME insurance types
- **Policy Lifecycle** — customers apply for policies; agents/admin approve or reject; automatic policy numbering, start/end dates, and expiry (PENDING → ACTIVE → EXPIRED/CANCELLED)
- **Claims Workflow** — customers file claims; agents review; admin settles (FILED → UNDER_REVIEW → APPROVED/REJECTED → SETTLED)
- **Premium Payments (simulated)** — due generation per premium frequency, payment history, overdue tracking
- **Agent–Customer Assignment** — admin assigns agents; agents see only their customers' data
- **In-App Notifications** — policy, claim, and payment events notify the affected users
- **Role-Based Dashboards** — live statistics for each role

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 17, Spring Boot 3 (Web, Data JPA, Validation), Spring Security 6 + JJWT |
| Frontend | React 18 (Vite), Axios, React Router |
| Database | MySQL 8 (normalized schema with foreign keys) |
| Build/Tools | Maven, npm, Git, Postman |

## Project Structure

```
insurance-management-system/
├── backend/          # Spring Boot REST API (Phase 2)
├── frontend/         # React SPA (Phase 3)
├── database/         # MySQL schema script
│   └── schema.sql
└── docs/             # SRS, Design Document, UML & ER diagrams
    ├── SRS-Insurance-Management-System.docx
    ├── Design-Document-UML-ER.docx
    └── diagrams/     # PNG diagrams + PlantUML sources
```

## Development Roadmap

- [x] **Phase 1 — Planning & Design**: SRS, UML diagrams, ER diagram, database schema
- [ ] **Phase 2 — Backend**: Spring Boot entities, repositories, services, controllers, JWT security
- [ ] **Phase 3 — Frontend**: React authentication, dashboards, CRUD screens
- [ ] **Phase 4 — Integration**: connect React to Spring Boot, end-to-end API testing
- [ ] **Phase 5 — Deployment & Documentation**: final docs, screenshots, demo

## Getting Started

> Full setup instructions will be added as Phases 2–4 are completed.

1. **Database**: install MySQL 8 Community Edition and run `database/schema.sql`
2. **Backend** (Phase 2): `cd backend && mvn spring-boot:run` — API at `http://localhost:8080`
3. **Frontend** (Phase 3): `cd frontend && npm install && npm run dev` — UI at `http://localhost:5173`

Default admin login (seeded): `admin@ims.com` / `Admin@123`

## Documentation

- [Software Requirements Specification](docs/SRS-Insurance-Management-System.docx)
- [Design Document — UML & ER Diagrams](docs/Design-Document-UML-ER.docx)
- Diagrams (PNG): [docs/diagrams](docs/diagrams)
