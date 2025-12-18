package com.slashdata.vehicleportal.dto;

public class AiBatchMatchRequest {

    private int page = 0;
    private int size = 20;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
