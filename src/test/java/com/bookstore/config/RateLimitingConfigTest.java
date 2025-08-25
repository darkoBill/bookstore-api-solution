package com.bookstore.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingConfigTest {

    @Test
    void bucketShouldBeEvictedAfterTtlAndRateLimitingReset() throws Exception {
        RateLimitingConfig config = new RateLimitingConfig(Duration.ofMillis(100), 1000) {
            @Override
            protected Bucket createNewBucket() {
                Bandwidth limit = Bandwidth.classic(1, Refill.intervally(1, Duration.ofHours(1)));
                return Bucket.builder().addLimit(limit).build();
            }
        };

        HandlerInterceptor interceptor = config.new RateLimitingInterceptor();
        String clientIp = "127.0.0.1";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(clientIp);

        MockHttpServletResponse response1 = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(request, response1, new Object())).isTrue();

        // Second request immediately should be rate limited
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(request, response2, new Object())).isFalse();

        Bucket firstBucket = config.getCache().getIfPresent(clientIp);

        Thread.sleep(200); // wait for TTL expiration

        MockHttpServletResponse response3 = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(request, response3, new Object())).isTrue();

        Bucket secondBucket = config.getCache().getIfPresent(clientIp);
        assertThat(secondBucket).isNotSameAs(firstBucket);
    }
}
