package com.slashdata.vehicleportal.dto;

import java.util.List;

public class BulkUploadResult<T> {

    private List<T> records;
    private int recordsAdded;
    private int recordsSkipped;
    private String message;
    private List<String> skipReasons;

    public BulkUploadResult() {
    }

    public BulkUploadResult(List<T> records, int recordsAdded, int recordsSkipped) {
        this(records, recordsAdded, recordsSkipped, null, List.of());
    }

    public BulkUploadResult(List<T> records, int recordsAdded, int recordsSkipped, String message, List<String> skipReasons) {
        this.records = records;
        this.recordsAdded = recordsAdded;
        this.recordsSkipped = recordsSkipped;
        this.message = message;
        this.skipReasons = skipReasons;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
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

    public List<String> getSkipReasons() {
        return skipReasons;
    }

    public void setSkipReasons(List<String> skipReasons) {
        this.skipReasons = skipReasons;
    }
}
