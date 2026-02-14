package com.example.consultant_backend.auth.config;

import com.example.consultant_backend.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for REST APIs with JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure endpoint authorization
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - No authentication required
                        .requestMatchers(
                                // Auth endpoints
                        "/api/auth/**",
                                "/api/auth/user/register",
                                "/api/auth/user/verify-otp",
                                "/api/auth/user/login",
                                "/api/auth/user/google-signin",
                                "/api/auth/user/signup",
                                "/api/auth/user/forgot-password",
                                "/api/auth/user/reset-password",
                                "/api/auth/doctor/login",
                                "/api/auth/doctor/verify-login-otp",
                                "/api/auth/doctor/google-signin",
                                "/api/auth/doctor/forgot-password",
                                "/api/auth/doctor/reset-password",

                                // Doctor list (public)
                                "/api/doctors/all",

                                // Swagger UI endpoints
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/health",

                                // Error page
                                "/error"
                        ).permitAll()

                        // Protected endpoints - JWT authentication required
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/appointments/**").authenticated()
                        .requestMatchers("/api/prescription/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Stateless session (no server-side session storage)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add JWT filter before Spring Security's default authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
