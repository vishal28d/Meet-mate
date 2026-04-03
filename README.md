# MeetMate

AI meeting summary generator with live transcript capture, Gemini summarization, and email delivery.

## Structure
- frontend: Next.js app for meeting UI
- backend: Spring Boot API + Kafka workers

## Quick start (local)
1. Start infra: `docker-compose up -d`
2. Backend: `cd backend` then `mvn spring-boot:run`
3. Frontend: `cd frontend` then `npm install` and `npm run dev`

## Environment
Copy `.env.example` into backend and frontend as needed.
