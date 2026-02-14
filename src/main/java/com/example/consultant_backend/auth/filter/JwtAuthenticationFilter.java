package com.example.consultant_backend.auth.filter;

import com.example.consultant_backend.auth.service.JwtService;
import com.example.consultant_backend.model.User;
import com.example.consultant_backend.repo.UserRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepo userRepo;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ✅ Skip JWT validation for public endpoints
        String path = request.getRequestURI();
        String method = request.getMethod();

        log.debug("Processing request: {} {}", method, path);  // ✅ Add logging

        if (isPublicEndpoint(path)) {
            log.debug("Public endpoint detected, skipping JWT validation: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No JWT token found in request headers for: {}", path);
                filterChain.doFilter(request, response);
                return;
            }

            // Remove "Bearer " prefix
            String token = authHeader.substring(7);

            // Extract USER ID from token (not email)
            Long userId = jwtService.extractUserId(token);

            // If userId exists and no authentication is set yet
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Validate token
                if (jwtService.validateToken(token)) {

                    // Load user details using userId
                    User user = userRepo.findById(userId).orElse(null);

                    if (user != null) {
                        // Create authentication token with userId as principal
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userId.toString(),
                                        null,
                                        Collections.emptyList()
                                );

                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("JWT authentication successful for userId: {}", userId);
                    } else {
                        log.warn("User not found for userId: {}", userId);
                    }
                } else {
                    log.warn("Invalid JWT token for userId: {}", userId);
                }
            }

        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * ✅ Check if endpoint is public (doesn't need JWT)
     */
    private boolean isPublicEndpoint(String path) {
        // ✅ Match all auth endpoints more broadly
        if (path.startsWith("/api/auth/")) {
            log.debug("Auth endpoint detected: {}", path);
            return true;
        }

        // Other public endpoints
        return path.equals("/api/doctors") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.equals("/swagger-ui.html") ||
                path.equals("/error") || path.equals("/health")||
                path.equals("/actuator/health");
    }
}
