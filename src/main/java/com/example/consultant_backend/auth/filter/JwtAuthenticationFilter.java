package com.example.consultant_backend.auth.filter;

import com.example.consultant_backend.auth.service.JwtService;
import com.example.consultant_backend.model.Doctor;
import com.example.consultant_backend.model.User;
import com.example.consultant_backend.repo.DoctorRepo;
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
    private final DoctorRepo doctorRepo;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.debug("Processing request: {} {}", method, path);

        // ✅ Skip JWT validation for public endpoints
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

            // Validate token first
            if (!jwtService.validateToken(token)) {
                log.warn("Invalid JWT token for path: {}", path);
                filterChain.doFilter(request, response);
                return;
            }

            // Only proceed if no authentication is set yet
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                // ✅ Check if this is a doctor endpoint
                if (path.startsWith("/api/doctors/")) {
                    authenticateDoctor(token, request);
                } else {
                    // ✅ Default to user authentication
                    authenticateUser(token, request);
                }
            }

        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * ✅ Authenticate Doctor (using email)
     */
    private void authenticateDoctor(String token, HttpServletRequest request) {
        try {
            String email = jwtService.extractEmail(token);

            if (email != null) {
                Doctor doctor = doctorRepo.findByEmail(email).orElse(null);

                if (doctor != null) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    email,  // Store email as String
                                    null,
                                    Collections.emptyList()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("✅ Doctor authentication successful for email: {}", email);
                } else {
                    log.warn("❌ Doctor not found for email: {}", email);
                }
            } else {
                log.warn("❌ Could not extract email from doctor token");
            }
        } catch (Exception e) {
            log.error("❌ Doctor authentication failed: {}", e.getMessage());
        }
    }

    /**
     * ✅ Authenticate User (using userId)
     */
    private void authenticateUser(String token, HttpServletRequest request) {
        try {
            Long userId = jwtService.extractUserId(token);

            if (userId != null) {
                User user = userRepo.findById(userId).orElse(null);

                if (user != null) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userId,  // Store userId as Long
                                    null,
                                    Collections.emptyList()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("✅ User authentication successful for userId: {}", userId);
                } else {
                    log.warn("❌ User not found for userId: {}", userId);
                }
            } else {
                log.warn("❌ Could not extract userId from user token");
            }
        } catch (Exception e) {
            log.error("❌ User authentication failed: {}", e.getMessage());
        }
    }

    /**
     * ✅ Check if endpoint is public (doesn't need JWT)
     */
    private boolean isPublicEndpoint(String path) {
        // ✅ Match all auth endpoints
        if (path.startsWith("/api/auth/")) {
            return true;
        }

        // ✅ Public doctor endpoints (GET only)
        if (path.equals("/api/doctors") || path.matches("/api/doctors/\\d+")) {
            return true;
        }

        // ✅ Other public endpoints
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.equals("/swagger-ui.html") ||
                path.equals("/error") ||
                path.equals("/health") ||
                path.startsWith("/dev") ||
                path.equals("/actuator/health");
    }
}
