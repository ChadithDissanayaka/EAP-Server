package com.automobileproject.eap.config;

import com.automobileproject.eap.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Step 1-2: Skip if header missing or not Bearer
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        // Handle Swagger UI sending "Bearer <token>" in the token field (double Bearer)
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        // Step 3-4: Skip if token is invalid
        if (!jwtUtil.isValid(token)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // Step 5: Extract claims
            Claims claims = jwtUtil.extractAllClaims(token);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            // Step 6: Build authentication token with role authority
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            // Step 7: Set in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("JWT authentication set for user: {} with role: {}", email, role);

        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
        }

        // Step 8: Continue filter chain
        chain.doFilter(request, response);
    }
}