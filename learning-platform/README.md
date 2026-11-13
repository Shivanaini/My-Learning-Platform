# LearnHub – Learning Platform (Spring Boot Microservices)

A Maven multi-module Spring Boot project. Users log in through a web UI and open the
**Lectures** module, where instructors upload lectures (video, audio, PDF, slides…)
and everyone can browse, search, stream and download them.

## Architecture

```
                 Browser  ──►  http://localhost:8080
                                     │
                              ┌──────▼──────┐
                              │ api-gateway │  :8080  (Spring Cloud Gateway)
                              └──────┬──────┘
          /api/auth/**               │ /api/lectures/**            /** (pages)
     ┌───────────────────┐   ┌───────▼─────────┐        ┌──────────────┐
     │   auth-service    │   │ lecture-service │        │  ui-service  │
     │ :8081  login/JWT  │   │ :8082 upload/   │        │ :8083 HTML/  │
     │ users DB          │   │ stream, files   │        │ CSS/JS pages │
     └───────────────────┘   └─────────────────┘        └──────────────┘
                 ▲ all services register with ▼
                       discovery-server :8761 (Eureka)
```

| Module | Port | What it does |
|---|---|---|
| `discovery-server` | 8761 | Eureka service registry |
| `api-gateway` | 8080 | Single entry point; routes requests to the services |
| `auth-service` | 8081 | Register / login, BCrypt passwords, issues JWT tokens |
| `lecture-service` | 8082 | Upload, list, search, stream, download, delete lectures |
| `ui-service` | 8083 | Login page + Lectures dashboard (plain HTML/CSS/JS, no Node needed) |

**Security:** `auth-service` signs a JWT (HS256). `lecture-service` verifies it with the
same shared secret (`app.jwt.secret` / `JWT_SECRET` env var). Only `INSTRUCTOR` users can
upload or delete; `STUDENT` users can view and download.

## Requirements

- Java 17+
- Maven 3.8+
- (Optional) Docker, if you want to run with MySQL via Docker Compose

## Run locally (no database install needed)

By default each service uses a file-based **H2** database (saved in `./data`) and lecture
files are stored in `./uploads`.

```bash
# 1. Build everything (from the project root)
mvn clean package -DskipTests

# 2. Start each service in its own terminal, in this order
java -jar discovery-server/target/discovery-server.jar
java -jar auth-service/target/auth-service.jar
java -jar lecture-service/target/lecture-service.jar
java -jar ui-service/target/ui-service.jar
java -jar api-gateway/target/api-gateway.jar
```

Or use the helper script: `start-all.bat` (Windows) / `./start-all.sh` (Mac/Linux).

Wait ~30 seconds for all services to register with Eureka (check http://localhost:8761),
then open **http://localhost:8080**.

You can also run each `*Application` class directly from IntelliJ / Eclipse / STS.

### Demo accounts (created automatically)

| Role | Username | Password |
|---|---|---|
| Instructor (can upload) | `instructor` | `instructor123` |
| Student (view only) | `student` | `student123` |

New users can also register from the login page.

## Run with Docker + MySQL

```bash
mvn clean package -DskipTests
docker compose up --build
```

Open http://localhost:8080. To use MySQL without Docker, start the services with
`--spring.profiles.active=mysql` and set `DB_HOST`, `DB_USER`, `DB_PASSWORD` if needed
(databases `learning_auth` and `learning_lectures` are created automatically).

## REST API (through the gateway, port 8080)

All `/api/lectures/**` calls need `Authorization: Bearer <token>`.

| Method | URL | Description |
|---|---|---|
| POST | `/api/auth/register` | `{username, fullName, email, password, role}` → token |
| POST | `/api/auth/login` | `{username, password}` → token |
| GET | `/api/auth/me` | Current user |
| GET | `/api/lectures?q=&course=` | List / search lectures |
| GET | `/api/lectures/courses` | Distinct course names |
| GET | `/api/lectures/{id}` | Lecture details |
| POST | `/api/lectures` | Upload (multipart: `file`, `title`, `course`, `description`) – instructor |
| GET | `/api/lectures/{id}/content` | Stream file inline (supports Range for video seeking) |
| GET | `/api/lectures/{id}/download` | Download file |
| DELETE | `/api/lectures/{id}` | Delete lecture – instructor |

Example with curl:

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"instructor","password":"instructor123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl -X POST localhost:8080/api/lectures -H "Authorization: Bearer $TOKEN" \
  -F "file=@intro.mp4" -F "title=Intro to Spring" -F "course=Java Microservices"
```

## Configuration you may want to change

| Property | Where | Default |
|---|---|---|
| `app.jwt.secret` / `JWT_SECRET` | auth-service **and** lecture-service (must match) | demo value – **change for production** |
| `app.storage.location` / `STORAGE_LOCATION` | lecture-service | `./uploads` |
| `spring.servlet.multipart.max-file-size` | lecture-service | `1GB` |
| `EUREKA_URL` | all clients | `http://localhost:8761/eureka` |

## Project structure

```
learning-platform/
├── pom.xml                      (parent: Spring Boot 3.3.5, Spring Cloud 2023.0.3)
├── discovery-server/
├── api-gateway/
├── auth-service/
│   └── controller, dto, model, repository, security (JwtService), service, config
├── lecture-service/
│   └── controller, dto, model, repository, security (JwtAuthFilter), service (FileStorageService)
├── ui-service/
│   └── resources/static/  index.html (login), dashboard.html (lectures), css/, js/
└── docker-compose.yml
```

## Ideas for next modules

Courses & enrolment, quizzes, progress tracking per lecture, comments/Q&A, storing files in
S3/MinIO instead of local disk, and a Config Server for centralised configuration.
