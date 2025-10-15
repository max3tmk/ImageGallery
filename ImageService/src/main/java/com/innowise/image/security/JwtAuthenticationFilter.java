package com.innowise.image.security;

import com.innowise.common.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String MISSING_OR_INVALID_AUTHENTICATION_HEADER = "Missing or invalid Authorization header";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isAuthPath(path)) {
            handleAuthPath(request, response, filterChain);
            return;
        }

        String token = extractToken(request, response);
        if (token == null) {
            return;
        }

        if (!isUserAuthorizedForPath(path, token, response)) {
            return;
        }

        UsernamePasswordAuthenticationToken authToken = createAuthenticationToken(token);
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private boolean isAuthPath(String path) {
        return path.startsWith("/api/auth/");
    }

    private void handleAuthPath(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            AnonymousAuthenticationToken anon = new AnonymousAuthenticationToken(
                    "anonymousKey",
                    "anonymousUser",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );
            SecurityContextHolder.getContext().setAuthentication(anon);
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, MISSING_OR_INVALID_AUTHENTICATION_HEADER);
            return null;
        }
        return authHeader.substring(7).trim();
    }

    private boolean isUserAuthorizedForPath(String path, String token, HttpServletResponse response) throws IOException {
        try {
            String username = jwtUtil.extractUsername(token);
            UUID userId = jwtUtil.extractUserId(token);

            if (username == null || userId == null) {
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, MISSING_OR_INVALID_AUTHENTICATION_HEADER);
                return false;
            }

            if (!isTokenValid(token, username)) {
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, MISSING_OR_INVALID_AUTHENTICATION_HEADER);
                return false;
            }

            if (isRestrictedImagePath(path)) {
                return isUserAllowedForImageAccess(path, userId, response);
            }

            return true;
        } catch (Exception ex) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, MISSING_OR_INVALID_AUTHENTICATION_HEADER);
            return false;
        }
    }

    private boolean isTokenValid(String token, String username) {
        try {
            return jwtUtil.validateToken(token);
        } catch (NoSuchMethodError | AbstractMethodError e) {
            return jwtUtil.validateToken(token, username);
        }
    }

    private boolean isRestrictedImagePath(String path) {
        return path.matches("/api/user/.+/images.*");
    }

    private boolean isUserAllowedForImageAccess(String path, UUID userId, HttpServletResponse response) throws IOException {
        String[] segments = path.split("/");
        if (segments.length > 3) {
            String pathUserId = segments[3];
            if (!userId.toString().equals(pathUserId)) {
                sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return false;
            }
        }
        return true;
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(String token) {
        String username = jwtUtil.extractUsername(token);
        return new UsernamePasswordAuthenticationToken(username, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(JSON_CONTENT_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String payload = "{\"message\":\"" + escapeJson(message) + "\"}";
        response.getWriter().write(payload);
        response.getWriter().flush();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}