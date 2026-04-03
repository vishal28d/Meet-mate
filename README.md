# MeetMate

MeetMate is an AI meeting assistant that captures transcripts in real time, generates structured summaries, and delivers outcomes to participants. The system combines a web UI for live collaboration with a Spring Boot backend that persists meeting data, publishes events, and integrates with external services such as Gemini for summarization and SMTP for email delivery.

## Features
- Live meeting transcripts streamed to connected clients over WebSockets
- Persistent storage of meetings, transcripts, and summaries in MongoDB
- Redis-backed cache for active meeting state
- Asynchronous eventing using Kafka for summary generation and downstream workflows
- AI-generated summaries and action items via Gemini
- Email notifications for meeting outcomes

## Architecture
The project is organized as a multi-service workspace with a frontend UI and a backend API. Supporting infrastructure (MongoDB, Redis, Kafka, Zookeeper) is provided via Docker Compose for local development.

### Frontend
- Framework: Next.js
- Responsibilities: meeting UI, transcript display, and live updates
- Entry point: [frontend/src/app/page.tsx](frontend/src/app/page.tsx)

### Backend
- Framework: Spring Boot
- Responsibilities: meeting lifecycle, transcript ingestion, summary generation, notifications
- API and WebSocket endpoints exposed on port 8080
- Persistence: MongoDB
- Caching: Redis
- Messaging: Kafka

## Repository Structure
- frontend: Next.js web application
- backend: Spring Boot API and background workers
- docker-compose.yml: local infrastructure services (MongoDB, Redis, Kafka, Zookeeper)

## Prerequisites
- Java 17
- Maven 3.9+
- Node.js 18+
- Docker Desktop (for local infrastructure)

## Local Development

### 1) Start infrastructure services
Run Docker Compose to start MongoDB, Redis, and Kafka:

```bash
docker-compose up -d
```

### 2) Start the backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on `http://localhost:8080` and exposes REST and WebSocket endpoints.

### 3) Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:3000`.

## Configuration
The backend uses environment variables for external dependencies and credentials. You can supply them through your shell environment or a local `.env` file (if your tooling loads it).

### Backend Environment Variables
- `MONGODB_URI` (default: `mongodb://localhost:27017/meetassistant`)
- `KAFKA_BOOTSTRAP` (default: `localhost:9092`)
- `REDIS_HOST` (default: `localhost`)
- `REDIS_PORT` (default: `6379`)
- `SMTP_HOST` (default: `localhost`)
- `SMTP_PORT` (default: `587`)
- `SMTP_USERNAME` (optional)
- `SMTP_PASSWORD` (optional)
- `SMTP_FROM` (default: `no-reply@meetassistant.local`)
- `SMTP_AUTH` (default: `false`)
- `SMTP_STARTTLS` (default: `true`)
- `GEMINI_API_KEY` (required for summary generation)

### AI Summarization
Summary generation requires `GEMINI_API_KEY`. If it is not provided, the application will still start, but summary requests will fail with a clear error.

## Build

### Backend

```bash
cd backend
mvn clean package
```

### Frontend

```bash
cd frontend
npm run build
```

## Operational Notes
- Kafka must be running to avoid consumer warnings on startup. Use `docker-compose up -d` if you are developing locally.
- The backend defaults to development-friendly configuration when optional environment variables are not set.

## License
Add your license information here.
