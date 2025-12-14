# SlashData Vehicle Portal Backend

This project provides a Spring Boot 3 backend for the SlashData Vehicle Portal. It includes JWT-based authentication, role-protected endpoints, and starter endpoints for managing users, vehicle master data, and ADP mappings.

## Running locally

```
mvn spring-boot:run
```

Datasource defaults can be overridden with the following environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `GEMINI_API_KEY`
