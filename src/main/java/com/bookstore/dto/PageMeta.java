package com.bookstore.dto;

public record PageMeta(
    int page,
    int size,
    long total,
    int totalPages
) {
    public static PageMeta of(int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return new PageMeta(page, size, total, totalPages);
    }
}