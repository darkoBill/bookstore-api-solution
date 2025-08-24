package com.bookstore.exception;

import java.util.UUID;

public class InsufficientInventoryException extends RuntimeException {
    
    private final UUID bookId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;
    
    public InsufficientInventoryException(UUID bookId, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("Insufficient inventory for book %s. Requested: %d, Available: %d", 
              bookId, requestedQuantity, availableQuantity));
        this.bookId = bookId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public UUID getBookId() {
        return bookId;
    }
    
    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}