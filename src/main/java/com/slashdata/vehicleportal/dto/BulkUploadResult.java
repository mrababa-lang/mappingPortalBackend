package com.slashdata.vehicleportal.dto;

import java.util.List;

public class BulkUploadResult<T> {

    private List<T> records;
    private int recordsAdded;
    private int recordsSkipped;

    public BulkUploadResult() {
    }

    public BulkUploadResult(List<T> records, int recordsAdded, int recordsSkipped) {
        this.records = records;
        this.recordsAdded = recordsAdded;
        this.recordsSkipped = recordsSkipped;
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
}
