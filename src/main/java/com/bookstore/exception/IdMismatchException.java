package com.bookstore.exception;

import java.util.UUID;

public class IdMismatchException extends RuntimeException {
    
    private final UUID pathId;
    private final UUID bodyId;
    
    public IdMismatchException(UUID pathId, UUID bodyId) {
        super(String.format("Path ID %s does not match body ID %s", pathId, bodyId));
        this.pathId = pathId;
        this.bodyId = bodyId;
    }
    
    public UUID getPathId() {
        return pathId;
    }
    
    public UUID getBodyId() {
        return bodyId;
    }
}