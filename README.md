# Music Streaming App

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-wrapper-C71A36?logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-required-4169E1?logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-migrations-CC0200?logo=flyway&logoColor=white)
![React](https://img.shields.io/badge/React-19.2-61DAFB?logo=react&logoColor=111111)
![TypeScript](https://img.shields.io/badge/TypeScript-6.0-3178C6?logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-8.0-646CFF?logo=vite&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-4.3-06B6D4?logo=tailwindcss&logoColor=white)
![Bun](https://img.shields.io/badge/Bun-package%20scripts-000000?logo=bun&logoColor=white)
![Cloudflare R2](https://img.shields.io/badge/Cloudflare%20R2-audio%20storage-F38020?logo=cloudflare&logoColor=white)

A full-stack music streaming application with a Spring Boot backend, PostgreSQL/Flyway database, and React + TypeScript frontend. The app focuses on browsing a music catalog, searching songs/artists, authentication, liked songs, audio playback, and optional Cloudflare R2-backed audio import/storage.

## Tech Stack

Backend:
- Java 17
- Spring Boot 3.5.6
- Spring Web, Spring Security, OAuth2 Client, Validation, Actuator
- Spring Data JPA + Hibernate
- PostgreSQL
- Flyway migrations
- Lombok
- JJWT
- AWS SDK S3 for Cloudflare R2
- Maven Wrapper

Frontend:
- React 19
- TypeScript 6
- Vite 8
- Tailwind CSS 4 via `@tailwindcss/vite`
- Framer Motion
- Heroicons
- Bun for package scripts

## Repository Structure

```text
.
├── backend/
│   └── music-streaming-app/      Spring Boot backend
├── frontend/
│   └── music-streaming-app/      React + TypeScript frontend
├── AGENTS.md                     Contributor and coding-agent guidelines
├── LICENSE
└── README.md
```

Important backend paths:

```text
backend/music-streaming-app/src/main/java/com/musicapp/backend
backend/music-streaming-app/src/main/resources/db/migration
backend/music-streaming-app/src/main/resources/db/undo
backend/music-streaming-app/src/test/java
```

Important frontend paths:

```text
frontend/music-streaming-app/src/components
frontend/music-streaming-app/src/views
frontend/music-streaming-app/src/hooks
frontend/music-streaming-app/src/services
frontend/music-streaming-app/src/types
frontend/music-streaming-app/src/utils
```

## Prerequisites

Install these locally:

- Java 17 or newer
- PostgreSQL
- Bun
- Git

The backend uses the Maven Wrapper, so a separate Maven installation is not required.

## Backend Setup

1. Create a PostgreSQL database:

```sql
CREATE DATABASE music_db;
```

2. Create a local environment file from the example:

```powershell
Copy-Item backend/music-streaming-app/.env.example backend/music-streaming-app/.env
```

3. Fill in the required database variables in `backend/music-streaming-app/.env`:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=music_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

4. Optional OAuth/R2 settings:

```properties
FRONTEND_URL=http://localhost:5173
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

R2_ACCOUNT_ID=
R2_ACCESS_KEY=
R2_SECRET_ACCESS_KEY=
R2_BUCKET=music-streaming-audio
R2_ENDPOINT=
R2_PUBLIC_BASE_URL=
```

5. Run the backend:

```powershell
cd backend/music-streaming-app
.\mvnw.cmd spring-boot:run
```

The backend starts on:

```text
http://localhost:8080
```

Flyway runs automatically on startup and applies migrations from:

```text
backend/music-streaming-app/src/main/resources/db/migration
```

## Frontend Setup

1. Install dependencies:

```powershell
cd frontend/music-streaming-app
bun install
```

2. Start the dev server:

```powershell
bun run dev
```

The frontend starts on:

```text
http://localhost:5173
```

The API base URL is currently hardcoded in:

```text
frontend/music-streaming-app/src/services/api.ts
```

Current value:

```text
http://localhost:8080/api
```

## Common Commands

Backend:

```powershell
cd backend/music-streaming-app
.\mvnw.cmd compile
.\mvnw.cmd spotless:apply
.\mvnw.cmd spotless:check
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Frontend:

```powershell
cd frontend/music-streaming-app
bun run lint
bun run build
bun run dev
bun run preview
```

## Main API Areas

Public catalog:
- `GET /api/discover`
- `GET /api/discover?title=...`
- `GET /api/songs/{id}`
- `GET /api/songs/{id}/stream`
- `GET /api/songs/{id}/stream-url`
- `GET /api/artists`
- `GET /api/artists/{id}`
- `GET /api/albums/{id}`

Authentication:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /oauth2/authorization/google`

Authenticated user:
- `GET /api/me`
- `GET /api/me/liked-songs`
- `POST /api/me/liked-songs/{songId}`
- `DELETE /api/me/liked-songs/{songId}`

Admin:
- `POST /api/admin/songs/import-r2`

## Cloudflare R2 Notes

R2 is optional for local catalog browsing, but required for R2-backed audio import and streaming.

Set these variables in `backend/music-streaming-app/.env` when using R2:

```properties
R2_ACCOUNT_ID=
R2_ACCESS_KEY=
R2_SECRET_ACCESS_KEY=
R2_BUCKET=
R2_ENDPOINT=
R2_PUBLIC_BASE_URL=
```

`R2_PUBLIC_BASE_URL` is used to generate public audio URLs for imported objects and by the backfill migration for previously imported R2 songs.

## Testing and Formatting

Backend Java code must be formatted with Google Java Format through Spotless:

```powershell
cd backend/music-streaming-app
.\mvnw.cmd spotless:apply
.\mvnw.cmd test
```

Frontend changes should pass lint and build:

```powershell
cd frontend/music-streaming-app
bun run lint
bun run build
```

## License

This project is released under the [MIT License](./LICENSE).
