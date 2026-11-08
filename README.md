# Teamplaner

This team-management App is inspired by apps like SpielerPlus: users register, create or join teams,
the team owner publishes events (e.g. training sessions), and members accept or decline them.

This is a project with a focus on a clean REST API, a relational data model and real access control.

## Stack
Java 21 · Spring Boot · Spring Security (JWT) · Spring Data JPA / Hibernate · PostgreSQL · Flyway ·
React + TypeScript (Vite) · nginx · Docker Compose · Testcontainers · springdoc OpenAPI · GitHub Actions

Planned: RabbitMQ (notifications), microservice split, Kubernetes.

## Domain rules
- A user can belong to zero, one or many teams.
- Whoever creates a team becomes its **owner** and gets an invite code; others join with that code.
- Only the owner can publish events; only team members can see them.
- Members can accept, decline or answer "maybe" until the response deadline or the event start.

## Run it

You can start the project in two ways. Both give you the same app.

### Option A: Everything with Docker (one command)

Requires Docker only.

```bash
docker compose up --build
```

Then open **http://localhost:3000**, register, create a team and publish an event.

| Service | URL |
|---|---|
| Web app (React, served by nginx) | http://localhost:3000 |
| Backend API / Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL (for dev tools) | `localhost:5433` (user/db/password: `teamplaner`) |
| pgAdmin (optional) | `docker compose --profile tools up`, then http://localhost:5050 |

nginx serves the React app and forwards `/api/*` to the backend, so the browser only talks to one origin.
Set your own JWT secret with `cp .env.example .env` and edit it.

Stop everything with `docker compose down` (add `-v` to also delete the database).

### Option B: Development mode (without Docker for the app code)

Backend and frontend run directly on your machine with hot reload.

**Requirements:** Java 21 (JDK), Maven, Node.js 22, PostgreSQL

**1. Database**: create a user and an empty database once:
```sql
CREATE USER teamplaner WITH PASSWORD 'teamplaner';
CREATE DATABASE teamplaner OWNER teamplaner;
```
Flyway creates all tables on the first start. The database must be empty or only contain Flyway-managed tables.

No local PostgreSQL? Use the database container instead. It already has the same user, password and database name (`teamplaner`), but listens on port 5433:
```bash
docker compose up -d postgres
export DB_URL=jdbc:postgresql://localhost:5433/teamplaner
```

**2. Backend** (terminal 1)
```bash
cd backend
mvn spring-boot:run
```
This uses the defaults `localhost:5432`, database `teamplaner`, user `teamplaner`, password `teamplaner`,
which match step 1. If you used the database container, run the `export` line from step 1 first, in the same terminal.

*Using your own database or user instead?* Set these variables for the start command:
```bash
DB_URL=jdbc:postgresql://localhost:5432/<your-database> DB_USER=<your-user> DB_PASSWORD=<your-password> mvn spring-boot:run
```

**3. Frontend** (terminal 2)
```bash
cd frontend
npm install
npm run dev
```
Open **http://localhost:5173**. The dev server forwards `/api/*` to the backend on port 8080.

The database keeps its data between restarts. Only new Flyway migrations are applied on startup, so you never need to reset it.

### Tests

Need Docker; Testcontainers starts a real PostgreSQL for the tests.
```bash
cd backend && mvn verify
```

## API overview

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Register, returns a JWT |
| POST | `/api/auth/login` | Login, returns a JWT |
| GET | `/api/users/me` | Current user |
| POST | `/api/teams` | Create a team (caller becomes owner) |
| POST | `/api/teams/join` | Join a team with an invite code |
| GET | `/api/teams` | My teams |
| GET | `/api/teams/{id}` | One of my teams |
| GET | `/api/teams/{id}/members` | Members of a team |
| POST | `/api/teams/{id}/events` | Publish an event (owner only) |
| GET | `/api/teams/{id}/events` | Upcoming events of a team |
| GET | `/api/events` | Upcoming events of all my teams |
| GET | `/api/events/{id}` | One event |
| PUT | `/api/events/{id}/attendance` | Accept / decline / maybe |

Example:
```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"email":"coach@example.com","password":"password123","displayName":"Coach"}' | jq -r .accessToken)

curl -s -X POST localhost:8080/api/teams -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"name":"SG Example"}'
```

## Design notes
- **Schema ownership:** Flyway migrations (`db/migration`) own the schema; Hibernate only validates it (`ddl-auto: validate`).
- **Memberships** are a separate entity with a composite key, so a user can be in many teams with a role per team.
- **Authorization is enforced server-side** in one place (`TeamAccessService`), not just by hiding UI elements.
- **No N+1 queries:** lists use `JOIN FETCH`, attendance counts are loaded with one grouped query for all events.
- **Timestamps** are stored as `TIMESTAMPTZ` and handled as `Instant` (UTC).
- **Errors** are returned as RFC 7807 problem details.
- The JWT secret has a development default; set `JWT_SECRET` (32+ chars) for anything real.
