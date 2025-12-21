package com.slashdata.vehicleportal.dto;

public class AiConnectionResult {

    private String status;
    private long latency;

    public AiConnectionResult() {
    }

    public AiConnectionResult(String status, long latency) {
        this.status = status;
        this.latency = latency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }
}
