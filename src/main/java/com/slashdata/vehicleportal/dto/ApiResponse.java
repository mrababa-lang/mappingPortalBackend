package com.slashdata.vehicleportal.dto;

import org.springframework.data.domain.Page;

public class ApiResponse<T> {

    private T data;
    private Meta meta;

    public ApiResponse() {
    }

    public ApiResponse(T data) {
        this.data = data;
    }

    public ApiResponse(T data, Meta meta) {
        this.data = data;
        this.meta = meta;
    }

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> of(T data, Meta meta) {
        return new ApiResponse<>(data, meta);
    }

    public static <T> ApiResponse<?> fromPage(Page<T> page) {
        Meta meta = new Meta(page.getTotalElements(), page.getTotalPages(), page.getNumber() + 1);
        return new ApiResponse<>(page.getContent(), meta);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public static class Meta {
        private long totalItems;
        private int totalPages;
        private int currentPage;

        public Meta() {
        }

        public Meta(long totalItems, int totalPages, int currentPage) {
            this.totalItems = totalItems;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
        }

        public long getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(long totalItems) {
            this.totalItems = totalItems;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }
    }
}
