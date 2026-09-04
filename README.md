# Slackish Backend

REST API for Slackish, a lightweight team messaging app. Built with Spring Boot 3 and Java 21.

## Features
- Email/password accounts with server-side sessions and an API token endpoint for bots
- Multi-workspace (multi-tenant) model with workspaces, channels (public/private), and roles
- Channel messages with threaded replies
- Direct messages between members
- Workspace invitations (single and bulk)
- Admin console: member management, role changes, audit log, and workspace export

## Requirements
- Java 21
- Maven 3.9+

## Run
```bash
mvn spring-boot:run
```
The API starts on http://localhost:8080 using an in-memory H2 database that is seeded with sample
data on startup. The H2 console is available at http://localhost:8080/h2
(JDBC URL `jdbc:h2:mem:slackish`, user `sa`).

## Configuration
See `src/main/resources/application.yml` for server, datasource, session, and JWT settings.

## API overview
- `POST /api/auth/register|login|logout`, `GET /api/auth/me`, `POST /api/auth/forgot|reset`
- `POST /api/bot/token`
- `GET|POST /api/workspaces`, `GET /api/workspaces/{id}/members`
- `GET|POST /api/workspaces/{id}/channels`, `POST /api/channels/{id}/join`, `DELETE /api/channels/{id}`
- `GET|POST /api/channels/{id}/messages`, `GET /api/messages/{id}`, `GET /api/messages/{id}/thread`
- `GET|POST /api/dms`, `GET /api/dms/{id}`, `POST /api/dms/{id}/messages`
- `POST /api/workspaces/{id}/invitations[/bulk]`, `POST /api/invitations/accept`
- `GET /api/admin/workspaces/{id}/audit|export`, `POST .../change-role|deactivate-member`
