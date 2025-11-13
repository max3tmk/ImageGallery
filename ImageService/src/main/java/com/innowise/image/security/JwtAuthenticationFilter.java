package com.innowise.image.security;

import com.innowise.common.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_SCHEME = "Bearer ";
    private static final String HEADER_AUTH = "Authorization";
    private static final String ERROR_JSON_TEMPLATE = "{\"error\":\"%s\"}";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/actuator/") || path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (token == null) {
            respond401(response, "Missing or invalid Authorization header");
            return;
        }

        String username = jwtUtil.extractUsername(token);
        UUID tokenUserId = jwtUtil.extractUserId(token);
        boolean valid;
        try {
            valid = jwtUtil.validateToken(token);
        } catch (Exception e) {
            valid = false;
        }

        if (!valid || username == null || tokenUserId == null) {
            respond401(response, "Invalid or expired token");
            return;
        }

        if (isRestrictedImagePath(path) && !isUserIdMatch(path, tokenUserId)) {
            respond403(response, "Access denied");
            return;
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_AUTH);
        if (header == null || !header.startsWith(AUTH_SCHEME)) {
            return null;
        }
        return header.substring(AUTH_SCHEME.length()).trim();
    }

    private boolean isRestrictedImagePath(String path) {
        if (!path.startsWith("/api/user/")) return false;
        String[] seg = path.split("/");
        return seg.length >= 5 && "images".equals(seg[4]);
    }

    private boolean isUserIdMatch(String path, UUID tokenUserId) {
        String[] segments = path.split("/");
        if (segments.length > 3) {
            String pathUserId = segments[3];
            return tokenUserId.toString().equals(pathUserId);
        }
        return false;
    }

    private void respond401(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format(ERROR_JSON_TEMPLATE, escape(message)));
    }

    private void respond403(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(String.format(ERROR_JSON_TEMPLATE, escape(message)));
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}