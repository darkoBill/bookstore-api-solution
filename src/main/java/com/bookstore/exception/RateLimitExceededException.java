package com.bookstore.exception;

/**
 * Exception thrown when rate limit is exceeded for a client
 */
public class RateLimitExceededException extends RuntimeException {
    
    private final String clientIp;
    private final long retryAfterSeconds;
    
    public RateLimitExceededException(String clientIp, long retryAfterSeconds) {
        super(String.format("Rate limit exceeded for IP: %s. Try again in %d seconds", clientIp, retryAfterSeconds));
        this.clientIp = clientIp;
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}