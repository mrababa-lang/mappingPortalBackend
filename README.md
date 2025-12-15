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

## ADP master upload samples
The `/api/adp/master/upload` endpoint accepts either JSON (`application/json`) or CSV (`text/csv`).
Sample payloads that match the expected schema are available under `src/main/resources/samples/`:

- `adp-master.json`: JSON array of ADP master records.
- `adp-master.csv`: CSV with headers matching the ADP master fields (e.g., `adpMakeId, makeEnDesc, adpModelId`, etc.).

Both files contain two example records you can use to verify the upload functionality without crafting payloads manually.
