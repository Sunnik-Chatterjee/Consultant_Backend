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
    private final UserRepo userRepo;  // ✅ Add this

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip JWT validation for public endpoints
        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No JWT token found in request headers");
                filterChain.doFilter(request, response);
                return;
            }

            // Remove "Bearer " prefix
            String token = authHeader.substring(7);

            // ✅ Extract USER ID from token (not email)
            Long userId = jwtService.extractUserId(token);

            // If userId exists and no authentication is set yet
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Validate token
                if (jwtService.validateToken(token)) {  // ✅ Simplified - no email param needed

                    // ✅ Load user details using userId
                    User user = userRepo.findById(userId)
                            .orElse(null);

                    if (user != null) {
                        // Create authentication token with userId as principal
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userId.toString(),  // ✅ Use userId as principal
                                        null,               // Credentials (not needed)
                                        Collections.emptyList()  // Authorities (add roles later if needed)
                                );

                        // Set additional details
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // Set authentication in security context
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("JWT authentication successful for userId: {}", userId);
                    }
                } else {
                    log.warn("Invalid JWT token for userId: {}", userId);
                }
            }

        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
            // Continue without authentication - Spring Security will handle 401
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Check if endpoint is public (doesn't need JWT)
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
                path.equals("/api/doctors") ||  // ✅ Doctors list is public
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.equals("/swagger-ui.html") ||
                path.equals("/error");
    }
}
