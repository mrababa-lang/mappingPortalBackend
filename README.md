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

## Upload samples
Sample payloads that match each upload endpoint are available under `src/main/resources/samples/`.

### ADP master upload
The `/api/adp/master/upload` endpoint accepts either JSON (`application/json`) or CSV (`text/csv`).

- `adp-master.json`: JSON array of ADP master records.
- `adp-master.csv`: CSV with headers matching the ADP master fields (e.g., `adpMakeId, makeEnDesc, adpModelId`, etc.).

Both files contain two example records you can use to verify the upload functionality without crafting payloads manually.

### Make bulk upload
The `/api/makes/bulk` endpoint accepts JSON or CSV.

- `make-bulk.json`: array of make objects with `name` and optional `nameAr` fields.
- `make-bulk.csv`: CSV with a header row containing `name` and `name_ar` (Arabic name is optional).

### Model bulk upload
The `/api/models/bulk` endpoint accepts JSON or CSV and expects existing `makeId` and `typeId` values.

- `model-bulk.json`: array of model payloads. Replace the `<MAKE_ID>` and `<TYPE_ID>` placeholders with actual IDs before uploading.
- `model-bulk.csv`: CSV version with the same placeholders and headers `makeId,typeId,name,nameAr`.

Both model sample files are ready to upload after substituting the IDs that exist in your environment.

## API specification

The consolidated API surface used by the frontend views is documented in `docs/vehicle-portal-api.json`. The file captures endpoints, expected request bodies, and example responses grouped by page (e.g., Dashboard, ADP Mapping, Users). All endpoints follow the JWT requirement noted in the document: include `Authorization: Bearer <token>` on every request except login.
