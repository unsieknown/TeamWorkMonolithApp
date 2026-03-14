package com.mordiniaa.backend.security.utils;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;

@Slf4j
@Component
public class JwtUtils {

    @Value("${security.app.jwt.secret}")
    private String jwtSecret;

    public ResponseCookie getCookie(String cookieName, String value, Duration duration) {

        return ResponseCookie.from(cookieName, value)
                .maxAge(duration)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .build();
    }

    public Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
