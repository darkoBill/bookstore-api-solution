package com.bookstore.exception;

import java.util.UUID;

public class InvalidInventoryAdjustmentException extends RuntimeException {

    private final UUID bookId;
    private final int currentQuantity;
    private final int adjustment;

    public InvalidInventoryAdjustmentException(UUID bookId, int currentQuantity, int adjustment) {
        super(String.format("Inventory adjustment would result in negative stock for book %s", bookId));
        this.bookId = bookId;
        this.currentQuantity = currentQuantity;
        this.adjustment = adjustment;
    }

    public UUID getBookId() {
        return bookId;
    }

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public int getAdjustment() {
        return adjustment;
    }
}
