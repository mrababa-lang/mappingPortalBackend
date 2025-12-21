package com.slashdata.vehicleportal.dto;

public class AdpMasterBulkUploadResponse {

    private int recordsAdded;
    private int recordsSkipped;
    private int errorCount;
    private String message;

    public AdpMasterBulkUploadResponse() {
    }

    public AdpMasterBulkUploadResponse(int recordsAdded, int recordsSkipped, int errorCount, String message) {
        this.recordsAdded = recordsAdded;
        this.recordsSkipped = recordsSkipped;
        this.errorCount = errorCount;
        this.message = message;
    }

    public int getRecordsAdded() {
        return recordsAdded;
    }

    public void setRecordsAdded(int recordsAdded) {
        this.recordsAdded = recordsAdded;
    }

    public int getRecordsSkipped() {
        return recordsSkipped;
    }

    public void setRecordsSkipped(int recordsSkipped) {
        this.recordsSkipped = recordsSkipped;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
