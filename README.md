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
- [x] **Phase 2 — Backend**: Spring Boot entities, repositories, services, controllers, JWT security
- [x] **Phase 3 — Frontend**: React authentication, dashboards, CRUD screens
- [x] **Phase 4 — Integration**: connect React to Spring Boot, end-to-end API testing
- [x] **Phase 5 — Deployment & Documentation**: Docker + free-hosting config, deployment guide, API docs, final report

## Getting Started

### Prerequisites
- JDK 17 or later
- MySQL 8 Community Edition (running on `localhost:3306`)
- Node.js 18+ (for the frontend)

### Backend
```bash
cd backend
.\mvnw.cmd spring-boot:run        # Windows (use ./mvnw on Linux/macOS)
```
- API base URL: `http://localhost:8081/api`
- Swagger UI (interactive API documentation): `http://localhost:8081/swagger-ui.html`
- The database `insurance_db` and all tables are created automatically on first run
- Override DB credentials with environment variables `DB_USERNAME` / `DB_PASSWORD` (defaults: `root` / local dev password in `application.properties`)

### Frontend (Phase 3)
```bash
cd frontend
npm install
npm run dev                   # UI at http://localhost:5173
```

Default admin login (seeded automatically): `admin@ims.com` / `Admin@123`

## API Overview

| Area | Base Endpoint | Highlights |
|------|---------------|------------|
| Auth | `/api/auth` | `POST /register`, `POST /login` → JWT |
| Users | `/api/users` | Admin CRUD, agent creation, profile & password |
| Plans | `/api/plans` | Browse/filter; admin manages catalogue |
| Policies | `/api/policies` | Apply, approve/reject, cancel, list by role |
| Claims | `/api/claims` | File, review (agent), settle (admin) |
| Payments | `/api/payments` | Simulated premium payment, history |
| Assignments | `/api/assignments` | Admin assigns agents to customers |
| Notifications | `/api/notifications` | In-app notifications, unread count |
| Dashboard | `/api/dashboard/summary` | Role-specific statistics |

## Deployment

See **[DEPLOYMENT.md](DEPLOYMENT.md)** for local run, one-command Docker (`docker compose up`), and a step-by-step **free cloud deployment** guide (TiDB Cloud + Render + Vercel, no credit card).

## Documentation

- [Software Requirements Specification](docs/SRS-Insurance-Management-System.docx)
- [Design Document — UML & ER Diagrams](docs/Design-Document-UML-ER.docx)
- [API Documentation](docs/API-Documentation.docx)
- [Final Project Report](docs/Final-Project-Report.docx)
- [Screenshots Document](docs/Screenshots-Document.docx)
- Diagrams (PNG): [docs/diagrams](docs/diagrams)
