# Internet Usage Monitoring Service

HTTP service for internet usage analytics. Java 17, Spring Boot, PostgreSQL, Liquibase.

## Setup

1. Use the local Postgres on port **5444**. Database: `internet_monitoring`, user: `postgres`, password: `admin`.

```bash
docker-compose up -d
```

2. Copy the challenge CSV to `data/sessions.csv`.

3. Build:

```bash
mvn -q -DskipTests package
```

## Ingest the dataset

Liquibase creates the tables on startup.

```bash
java -jar target/internet-usage-monitoring-service-1.0-SNAPSHOT.jar ingest data/sessions.csv
```

## Start the API

```bash
java -jar target/internet-usage-monitoring-service-1.0-SNAPSHOT.jar
```

Swagger UI: http://localhost:8080/swagger-ui.html

## APIs

Top users for an as-of date (`DDMMYYYY`), ranked by last 30 days of session time:

```bash
curl "http://localhost:8080/analytics?date=24122022&pageSize=100&page=1"
```

User usage relative to `YYYYMMDDThhmm`:

```bash
curl "http://localhost:8080/user/search?username=brainyHeron5&datetime=20221104T1543"
```

Future dates return `422`. Unknown users return `404`. Empty pages return `{ "ok": true, "data": [] }`.

## Tests

```bash
mvn test
```

JaCoCo report: `target/site/jacoco/index.html`
