package com.slashdata.vehicleportal.dto;

public class AdpMasterBulkSyncResponse {

    private int recordsAdded;
    private int recordsSkipped;
    private String message;

    public AdpMasterBulkSyncResponse() {
    }

    public AdpMasterBulkSyncResponse(int recordsAdded, int recordsSkipped, String message) {
        this.recordsAdded = recordsAdded;
        this.recordsSkipped = recordsSkipped;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
