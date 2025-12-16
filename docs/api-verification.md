# API Verification Against Vehicle Portal Spec

This document cross-checks the published API specification (`docs/vehicle-portal-api.json`) with the Spring controllers currently in the backend. Each section lists whether the expected endpoints are implemented and where they live in the codebase.

## Login
- ✅ `/api/auth/login` implemented in `AuthController`.

## Dashboard
- ✅ `/api/stats/dashboard` implemented in `StatsController.dashboard()`.
- ✅ `/api/dashboard/trends` implemented in `DashboardController.trends()`.
- ✅ `/api/dashboard/activity` implemented in `DashboardController.activity()`.

## Vehicle Makes
- ✅ CRUD `/api/makes` implemented in `MakeController`.
- ✅ Bulk upload `/api/makes/bulk` implemented in `MakeBulkController`.

## Vehicle Models
- ✅ CRUD `/api/models` implemented in `ModelController`.
- ✅ Bulk upload `/api/models/bulk` implemented in `ModelBulkController`.

## Vehicle Types
- ✅ CRUD `/api/types` implemented in `VehicleTypeController`.

## ADP Master
- ✅ Paginated fetch `/api/adp/master` implemented in `ADPMasterController`.
- ✅ Upload `/api/adp/master/upload` implemented in `AdpPublicController`.

## ADP Mapping
- ✅ Filtered search `/api/adp/mappings` implemented in `ADPMappingController.search()`.
- ✅ Upsert `/api/adp/mappings/{adpId}` implemented in `ADPMappingController.upsert()`.
- ✅ Approve `/api/adp/mappings/{adpId}/approve` implemented in `ADPMappingController.approve()`.
- ✅ Reject `/api/adp/mappings/{adpId}/reject` implemented in `ADPMappingController.reject()`.
- ✅ Bulk action `/api/adp/mappings/bulk-action` implemented in `ADPMappingBulkController`.
- ✅ AI suggestion `/api/ai/suggest-mapping` implemented in `AiController.suggestMapping()`.

## ADP Makes (Unique)
- ✅ `/api/adp/makes` implemented in `ADPUniqueController.uniqueMakes()`.
- ✅ `/api/adp/makes/map` implemented in `ADPUniqueController.mapMake()`.

## ADP Types (Unique)
- ✅ `/api/adp/types` implemented in `ADPUniqueController.uniqueTypes()`.
- ✅ `/api/adp/types/map` implemented in `ADPUniqueController.mapType()`.

## Mapping Review
- ✅ Pending review listing is served via `ADPMappingController.search()` and review helpers in the same controller.
- ✅ Approve/Reject and bulk actions are available as noted in the ADP Mapping section.

## Users
- ✅ CRUD `/api/users` implemented in `UserController` (admin-only).

## Tracking
- ✅ Activity logs `/api/tracking/logs` implemented in `TrackingController`.
- ✅ ADP mapping query `/api/adp/mappings` is covered in the ADP Mapping section.

## Configuration
- ✅ `/api/config` GET/PUT implemented in `ConfigController`.

## Summary
All endpoints in the supplied specification are now covered by existing controllers.
