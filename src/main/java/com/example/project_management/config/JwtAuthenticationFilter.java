package com.example.project_management.config;

import com.example.project_management.service.UserDetailsServiceImpl;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // ✅ Skip authentication for signup & login
        if (path.startsWith("/auth/")) {
            log.debug("Skipping JWT filter for public endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String header = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // ✅ Extract JWT token
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            try {
                email = jwtUtil.extractEmail(token);
                log.debug("Extracted email from JWT: {}", email);
            } catch (Exception e) {
                log.error("Failed to extract email from token", e);
            }
        } else {
            log.warn("No Authorization header or not starting with Bearer");
        }

        // ✅ Validate and set Authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = userDetailsService.loadUserByUsername(email);
            log.debug("Loaded UserDetails for email: {}", email);

            if (jwtUtil.validateToken(token)) {
                log.debug("✅ JWT token validated successfully for {}", email);

                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                log.warn("❌ JWT token validation failed for {}", email);
            }
        } else if (email == null) {
            log.warn("❌ Email is null, cannot set authentication");
        } else {
            log.debug("Authentication already exists in SecurityContext");
        }

        filterChain.doFilter(request, response);
    }
}
