package com.mordiniaa.backend.security.service;

import com.mordiniaa.backend.security.token.JwtToken;
import com.mordiniaa.backend.security.utils.JwtUtils;
import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${security.app.jwt.tokenName}")
    private String tokenName;

    @Value("${security.app.jwt.minutesOfLife}")
    private long accessTtlMinutes;

    @Value("${security.app.jwt.issuer}")
    private String issuer;

    @Value("${security.app.jwt.audience}")
    private String audience;

    private final JwtUtils jwtUtils;

    public JwtToken buildJwt(String subject, String sessionId, List<String> roles) {

        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofMinutes(accessTtlMinutes));

        String role = (roles == null || roles.isEmpty()) ? null : roles.getFirst();

        String jwt = Jwts.builder()
                .issuer(issuer)
                .subject(subject)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))

                .claim("role", role)
                .claim("sid", sessionId)
                .signWith(jwtUtils.key())
                .compact();

        long ttl = exp.toEpochMilli() - now.toEpochMilli();
        return new JwtToken(tokenName, jwt, ttl);
    }

    public UUID extractUserId(Claims claims) {

        String stringId = claims.getSubject();

        try {
            return UUID.fromString(stringId);
        } catch (Exception e) {
            throw new RuntimeException(); // TODO: Change In Exceptions Section
        }
    }

    public UUID extractSessionId(Claims claims) {

        String stringId = (String) claims.get("sid");

        try {
            return UUID.fromString(stringId);
        } catch (Exception e) {
            throw new RuntimeException(); // TODO: Change In Exceptions Section
        }
    }

    public List<String> extractRoles(Claims claims) {

        String role = (String) claims.get("role");
        return List.of(role);
    }

    public Claims parseAndValidate(String jwtToken) {
        return Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

    public Claims parseAllowExpired(String jwtToken) {
        try {
            return parseAndValidate(jwtToken);
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public String parseJwtTokenFromCookie(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(tokenName))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
    }

    public JwtToken getEmptyToken() {
        return new JwtToken(tokenName, "", 0);
    }
}
