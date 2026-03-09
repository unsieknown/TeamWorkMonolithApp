package com.mordiniaa.backend.security.filters;

import com.mordiniaa.backend.config.JwtProperties;
import com.mordiniaa.backend.security.objects.JwtPrincipal;
import com.mordiniaa.backend.security.service.JwtService;
import com.mordiniaa.backend.security.service.SessionRedisService;
import com.mordiniaa.backend.security.service.token.RefreshTokenService;
import com.mordiniaa.backend.services.auth.SessionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${security.app.jwt.tokenName}")
    private String jwtTokenName;

    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;
    private final SessionRedisService sessionRedisService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        if (jwtProperties.getWhiteList().contains(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null) {
            String jwtToken = parseToken(request);

            if (jwtToken == null) {
                filterChain.doFilter(request, response);
                return;
            }

            try {
                Claims claims = jwtService.parseAndValidate(jwtToken);
                UUID sessionId = jwtService.extractSessionId(claims);
                if (!sessionRedisService.validateSession(sessionId)) {
                    if (!sessionService.validateSession(sessionId)) {
                        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(jwtTokenName).path("/").build().toString());
                        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenService.clearUserToken().toString());
                        throw new RuntimeException(); // TODO: Change In Exceptions Section
                    }
                }

                UUID userId = jwtService.extractUserId(claims);

                List<String> roles = jwtService.extractRoles(claims);
                JwtPrincipal principal = new JwtPrincipal(
                        userId,
                        sessionId,
                        roles
                );

                Collection<? extends GrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                authorities
                        );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authenticationToken);
            } catch (JwtException e) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String parseToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(jwtTokenName))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
