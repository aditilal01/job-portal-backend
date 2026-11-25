# Job Portal Backend

REST API for the Job Portal application, built with **Java 17**, **Spring Boot 3.2.5**, **Spring Security**, **JWT**, and **PostgreSQL**. It provides authentication, job management, applications, candidate profiles, saved jobs, recruiter/admin endpoints, and optional AI-powered resume tools.

Frontend repository: [job-portal-frontend](https://github.com/aditilal01/job-portal-frontend)

## Features

- JWT-based registration and login with role-based authorization (`CANDIDATE`, `RECRUITER`, `ADMIN`).
- Job creation, editing, deactivation, public discovery, filtering, pagination, and sorting.
- Candidate applications with resume upload and protected resume downloads.
- Recruiter dashboard and applicant/status management.
- Candidate profile management with an explicit recruiter-visibility setting.
- Saved jobs (add, list, remove).
- Admin statistics and user directory endpoints.
- Optional AI resume analysis and job matching using OpenAI.
- Swagger/OpenAPI documentation and centralized exception handling.

## Tech stack

| Area | Technology |
| --- | --- |
| Language / runtime | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security, JWT (JJWT 0.11.5) |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL 16 |
| Resume parsing | Apache Tika 2.9.2 |
| API documentation | SpringDoc OpenAPI 2.3.0 |
| Build | Maven |
| Tests | Spring Boot Test, Spring Security Test, H2 |

## Architecture

The application uses a layered Spring architecture:

- `controller` — REST endpoints
- `dto` — request and response objects
- `entity` — JPA domain entities
- `repository` — database access
- `service` — business logic
- `security` — JWT authentication and user details
- `config` — application and security configuration
- `exception` — exception handling
- `specification` — dynamic job-search filters

## Run locally with Docker Compose

### Prerequisites

- Docker Desktop or Docker Engine with the Compose plugin
- Git

### 1. Clone the repository

```bash
git clone https://github.com/aditilal01/job-portal-backend.git
cd job-portal-backend
```

### 2. Configure environment variables

```bash
cp .env.example .env
```

Edit `.env` and replace the placeholders. Generate a strong JWT secret, for example:

```bash
openssl rand -base64 48
```

Required values:

| Variable | Purpose |
| --- | --- |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | JWT signing secret (at least 32 bytes for HS256) |

Common optional settings:

| Variable | Default | Purpose |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | Host port for the API |
| `JWT_EXPIRATION_MS` | `86400000` | Token lifetime in milliseconds |
| `CORS_ORIGINS` | `http://localhost:5173` | Allowed frontend origin(s) |
| `OPENAI_API_KEY` | empty | Enables AI resume analysis and matching |
| `OPENAI_MODEL` | `gpt-4o-mini` | OpenAI model used by AI tools |

Do not commit `.env`, database passwords, JWT secrets, or API keys.

### 3. Start the services

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f backend
```

The Compose file starts PostgreSQL and the API. It also defines the frontend container build using the sibling `job-portal-frontend` directory; for a standalone backend checkout, run the frontend separately or clone both repositories side by side.

### 4. Open the API documentation

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Stop the services:

```bash
docker compose down
```

To also delete persisted PostgreSQL and resume-upload data, use `docker compose down -v`. **This permanently deletes those volumes.**

## Run without Docker (optional)

Requirements: JDK 17, Maven, and a running PostgreSQL instance.

Create a database named `jobportal`, configure the database and JWT environment variables expected by the application, then run:

```bash
mvn clean test -s settings.xml
mvn spring-boot:run -s settings.xml
```

The custom Maven `settings.xml` is included for environments that need the configured Maven mirror.

## API overview

All protected routes require:

```http
Authorization: Bearer <JWT>
```

| Area | Method | Endpoint | Access |
| --- | --- | --- | --- |
| Auth | POST | `/api/auth/register` | Public (candidate/recruiter) |
| Auth | POST | `/api/auth/login` | Public |
| Jobs | GET | `/api/jobs` | Public |
| Jobs | GET | `/api/jobs/{id}` | Public |
| Jobs | POST | `/api/jobs` | Recruiter |
| Jobs | PUT | `/api/jobs/{id}` | Owning recruiter |
| Jobs | DELETE | `/api/jobs/{id}` | Owning recruiter |
| Applications | POST | `/api/applications` | Candidate |
| Applications | GET | `/api/applications/my` | Candidate |
| Applications | GET | `/api/applications/job/{jobId}` | Owning recruiter |
| Applications | PATCH | `/api/applications/{id}/status` | Owning recruiter |
| Applications | GET | `/api/applications/{id}/resume` | Candidate owner / owning recruiter |
| Candidate profile | GET, PUT | `/api/candidate/profile` | Candidate |
| Saved jobs | POST | `/api/candidate/saved-jobs/{jobId}` | Candidate |
| Saved jobs | GET | `/api/candidate/saved-jobs` | Candidate |
| Saved jobs | DELETE | `/api/candidate/saved-jobs/{jobId}` | Candidate |
| AI tools | POST | `/api/candidate/ai/resume/analyze` | Candidate |
| AI tools | GET | `/api/candidate/ai/job-matches` | Candidate |
| Admin | GET | `/api/admin/stats` | Admin |
| Admin | GET | `/api/admin/users` | Admin |

Use Swagger for the authoritative request schemas and complete endpoint documentation. Public registration is intended for candidate and recruiter roles; admin accounts must be provisioned through a trusted administrative process.

### Job search

`GET /api/jobs` supports optional `title`, `skill`, `location`, and `experience` filters, plus `page`, `size`, `sortBy`, and `sortDir` parameters. The supported sort fields are `createdAt`, `title`, `location`, and `experienceRequired`.

### Application lifecycle

Applications begin in `APPLIED`. Supported progression includes `SHORTLISTED`, `INTERVIEW`, and `HIRED`, with `REJECTED` available from applicable stages. The service validates status transitions and prevents a candidate from applying to the same job more than once.

### Candidate profile privacy

Candidates control whether their professional profile is visible to recruiters through `profileVisibleToRecruiters`. Recruiter-facing application responses expose the permitted professional summary only when the candidate has enabled visibility.

### Resume uploads and AI tools

Application resumes and AI resume uploads accept PDF, DOC, and DOCX files, with a 10 MB upload limit. Apache Tika extracts resume text. AI analysis and job matching require `OPENAI_API_KEY` configured **on the backend only**. The API key must never be placed in frontend environment variables or committed to source control. AI-generated scores are advisory and should not be used as automatic hiring decisions.

## Testing

Run the backend test suite:

```bash
mvn test -s settings.xml
```

The project includes H2 and Spring Boot/Security test dependencies for automated tests.

## Security notes

- Keep `.env` and all credentials out of Git.
- Use a unique, strong JWT secret outside local development.
- Restrict CORS origins to trusted frontend origins.
- Keep AI provider keys on the server.
- Resume files are served through authorization-checked API routes; do not expose the upload directory as a public static folder.
- Use HTTPS and appropriate production database backups/deployment controls in production.

## License

No license has been specified for this repository. Contact the repository owner before reusing or redistributing the code.
