package com.bookstore.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;

@Configuration
@Slf4j
public class RateLimitingConfig implements WebMvcConfigurer {

    private final Cache<String, Bucket> cache;

    public RateLimitingConfig(
        @Value("${rate-limit.cache.ttl:PT1M}") Duration cacheTtl,
        @Value("${rate-limit.cache.max-size:10000}") long maxSize
    ) {
        this.cache = Caffeine.newBuilder()
            .expireAfterAccess(cacheTtl)
            .maximumSize(maxSize)
            .build();
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitingInterceptor()).addPathPatterns("/api/**");
    }
    
    protected Bucket createNewBucket() {
        // Allow 100 requests per minute per IP
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    Cache<String, Bucket> getCache() {
        return cache;
    }
    
    class RateLimitingInterceptor implements HandlerInterceptor {
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String clientIP = getClientIP(request);
            
            Bucket bucket = cache.get(clientIP, k -> createNewBucket());
            
            if (bucket.tryConsume(1)) {
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
                return true;
            } else {
                response.setStatus(429); // Too Many Requests
                response.addHeader("X-Rate-Limit-Retry-After", "60");
                response.getWriter().write("{\"error\":\"Rate limit exceeded. Try again later.\"}");
                log.warn("Rate limit exceeded for IP: {}", clientIP);
                return false;
            }
        }
        
        private String getClientIP(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty()) {
                return xRealIP;
            }
            
            return request.getRemoteAddr();
        }
    }
}