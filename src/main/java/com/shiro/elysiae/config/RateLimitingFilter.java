package com.shiro.elysiae.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 60_000;
    private final Map<String, ClientEntry> clients = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private record ClientEntry(AtomicInteger count, long windowStart) {}

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(clients::clear, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getServletPath();
        if (!path.startsWith("/api/auth/login") && !path.startsWith("/api/auth/change-password")) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = httpRequest.getRemoteAddr();
        long now = System.currentTimeMillis();

        ClientEntry entry = clients.compute(clientIp, (k, v) -> {
            if (v == null || now - v.windowStart() > WINDOW_MS) {
                return new ClientEntry(new AtomicInteger(1), now);
            }
            v.count().incrementAndGet();
            return v;
        });

        if (entry.count().get() > MAX_REQUESTS) {
            httpResponse.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
            httpResponse.getWriter().write("Too many requests. Please try again later.");
            return;
        }

        chain.doFilter(request, response);
    }
}
