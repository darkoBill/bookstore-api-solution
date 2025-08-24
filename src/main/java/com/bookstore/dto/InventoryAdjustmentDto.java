package com.bookstore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record InventoryAdjustmentDto(
    @NotNull UUID bookId,
    @NotNull Integer quantityChange,
    @NotNull AdjustmentType type,
    @Size(max = 500) String reason,
    Instant timestamp
) {
    
    public enum AdjustmentType {
        STOCK_RECEIVED,
        STOCK_DAMAGED,
        STOCK_LOST,
        STOCK_RETURNED,
        STOCK_SOLD,
        MANUAL_ADJUSTMENT
    }
}