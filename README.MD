# POS (Point-Of-Sale) Backend

A concise, production-ready Java Spring Boot backend for a Point-Of-Sale system. This service provides REST APIs for products, carts, sales (with Chapa payment integration), users and authentication (JWT). It ships with Docker and Docker Compose for easy local development.

---

## Key Features
- JWT-based authentication and authorization
- Product management (CRUD)
- Cart operations (add/remove items)
- Sales processing and Chapa payment integration
- Email notifications (SMTP)
- PostgreSQL persistence using Spring Data JPA
- OpenAPI / Swagger UI for interactive API documentation

## Tech Stack
- Java 21, Spring Boot 3
- Spring Web, Spring Data JPA, Spring Security
- PostgreSQL
- JWT (jjwt)
- SpringDoc OpenAPI (Swagger UI)
- Docker & Docker Compose

## Project Structure (high level)
- `src/main/java/com/pos/app` – application entry and packages
	- `controller/` – REST controllers (Auth, Product, Cart, Sale, User)
	- `service/` – business logic (Chapa, Email, Auth, Cart)
	- `repository/` – Spring Data JPA repositories
	- `security/` – JWT utilities and filters
	- `config/` – Spring configuration classes (security, OpenAPI, Chapa props)
- `src/main/resources/application.properties` – default configuration
- `Dockerfile` and `docker-compose.yml` – containerized setup

## Prerequisites
- Java 21 (or Docker)  
- Docker & Docker Compose (recommended for full-stack local run)  
- (Optional) Maven — the project includes the Maven wrapper `mvnw`

---

## Quick Start — Docker (recommended)
This will start PostgreSQL and the application in containers.

1. From the project root, build and start services:

```bash
docker compose up --build -d
```

2. Follow logs for the application:

```bash
docker compose logs -f app
```

3. Stop and remove containers when done:

```bash
docker compose down
```

Notes:
- The compose file exposes the app on port `8080` and Postgres on `5432`.
- Default DB credentials (development only): DB `pos_db`, user `pos_user`, password `pos_password`.
- You can override secrets by setting environment variables or creating an `.env` file with `JWT_SECRET`, `CHAPA_SECRET_KEY`, `CHAPA_CALLBACK_URL`, `CHAPA_RETURN_URL`.

Example `.env` (do not commit):

```env
JWT_SECRET=your_jwt_secret_here
CHAPA_SECRET_KEY=your_chapa_key_here
CHAPA_CALLBACK_URL=http://localhost:8080/api/sales/chapa/callback
CHAPA_RETURN_URL=http://localhost:8080/api/sales/chapa/return
```

---

## Run Locally (without Docker)
1. Ensure you have a PostgreSQL instance available and update env vars below or edit `src/main/resources/application.properties` accordingly.
2. Build the app:

```bash
./mvnw clean package -DskipTests
```

3. Run the JAR while passing required environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pos_db \
SPRING_DATASOURCE_USERNAME=pos_user \
SPRING_DATASOURCE_PASSWORD=pos_password \
JWT_SECRET=your_jwt_secret_here \
java -jar target/pos-0.0.1-SNAPSHOT.jar
```

---

## Configuration and Important Environment Variables
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` — database connection
- `JWT_SECRET` — secret used to sign JWT tokens
- `CHAPA_SECRET_KEY`, `CHAPA_CALLBACK_URL`, `CHAPA_RETURN_URL` — Chapa payment integration
- Mail settings (smtp) are defined in `application.properties` for outgoing emails — rotate or remove credentials before sharing

The application loads values from environment variables with sensible development defaults in `src/main/resources/application.properties`.

---

## API Documentation (Swagger)
Once the app is running, open the interactive API docs at:

`http://localhost:8080/swagger-ui.html`

---

## Database
- Uses PostgreSQL. When running with docker-compose the service name is `postgres` and the JDBC URL inside containers is `jdbc:postgresql://postgres:5432/pos_db`.
- JPA is configured with `spring.jpa.hibernate.ddl-auto=update` for development convenience.

---

## Useful Commands
- Build with Maven wrapper: `./mvnw clean package`
- Run (local): `java -jar target/pos-0.0.1-SNAPSHOT.jar`
- Docker compose up: `docker compose up --build -d`
- Follow app logs: `docker compose logs -f app`

---


