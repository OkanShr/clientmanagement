package com.xera.clientmanagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 3;
    private static final long TIME_WINDOW = 60 * 1000; // 1 minute in milliseconds

    private final Map<String, RateLimitInfo> ipRequestMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        if (isLoginRequest(request)) {
            if (isRateLimited(clientIp)) {
                response.setStatus(429);
                response.getWriter().write("Too many login attempts. Please try again later.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        // Check if the request is targeting the login endpoint
        return request.getRequestURI().equals("/api/v1/auth/signin");
    }

    private boolean isRateLimited(String clientIp) {
        RateLimitInfo rateLimitInfo = ipRequestMap.computeIfAbsent(clientIp, k -> new RateLimitInfo());

        long currentTime = Instant.now().toEpochMilli();
        long windowStartTime = rateLimitInfo.getWindowStartTime();

        // Check if the time window has expired
        if (currentTime - windowStartTime > TIME_WINDOW) {
            rateLimitInfo.resetWindow(currentTime);
        }

        // Check if the maximum attempts have been reached
        if (rateLimitInfo.getAttempts().incrementAndGet() > MAX_ATTEMPTS) {
            return true;
        }

        return false;
    }

    // Inner class to store rate limit information
    private static class RateLimitInfo {
        private final AtomicInteger attempts = new AtomicInteger(0);
        private long windowStartTime = Instant.now().toEpochMilli();

        public AtomicInteger getAttempts() {
            return attempts;
        }

        public long getWindowStartTime() {
            return windowStartTime;
        }

        public void resetWindow(long currentTime) {
            this.windowStartTime = currentTime;
            this.attempts.set(0);
        }
    }
}
