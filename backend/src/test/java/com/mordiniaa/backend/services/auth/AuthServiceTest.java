package com.mordiniaa.backend.services.auth;

import com.mordiniaa.backend.models.Session;
import com.mordiniaa.backend.repositories.mysql.RefreshTokenFamilyRepository;
import com.mordiniaa.backend.security.model.RefreshTokenEntity;
import com.mordiniaa.backend.security.model.RefreshTokenFamily;
import com.mordiniaa.backend.security.objects.JwtPrincipal;
import com.mordiniaa.backend.security.service.token.RawTokenService;
import com.mordiniaa.backend.security.utils.JwtUtils;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenFamilyRepository refreshTokenFamilyRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RawTokenService rawTokenService;

    @AfterEach
    void tearDown() {
        refreshTokenFamilyRepository.deleteAll();

        ScanOptions options = ScanOptions.scanOptions()
                .match("session:*")
                .build();

        assertNotNull(stringRedisTemplate.getConnectionFactory());
        Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options);

        List<String> keysToDelete = new ArrayList<>();
        while (cursor.hasNext())
            keysToDelete.add(new String(cursor.next(), StandardCharsets.UTF_8));

        if (!keysToDelete.isEmpty())
            stringRedisTemplate.delete(keysToDelete);
    }

    @Test
    @DisplayName("Authenticate Valid Test")
    void authenticateValidTest() {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        "superSecretPassword"
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");
        List<ResponseCookie> cookies = authService.authenticate(authentication, request);
        assertNotNull(cookies);
        assertFalse(cookies.isEmpty());
    }

    @Test
    @DisplayName("Refresh Token Valid Test")
    void refreshTokenValidTest() {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        "superSecretPassword"
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");
        List<ResponseCookie> cookies = authService.authenticate(authentication, request);

        ResponseCookie jwtCookie = cookies.stream().filter(cookie -> cookie.getName().equals("TEAMWORK-ACCESS"))
                .findFirst().orElseThrow();

        ResponseCookie refreshCookie = cookies.stream().filter(cookie -> cookie.getName().equals("TEAMWORK-REFRESH"))
                .findFirst().orElseThrow();

        String jwtToken = jwtCookie.getValue();
        String refreshToken = refreshCookie.getValue();
        UUID userId = UUID.fromString(Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().getSubject());

        UUID sessionId = UUID.fromString((String) Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().get("sid"));

        List<String> roles = List.of((String) Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().get("sid"));

        Authentication jwtAuthentication = new UsernamePasswordAuthenticationToken(
                new JwtPrincipal(userId, sessionId, roles),
                null,
                List.of()
        );

        List<ResponseCookie> refreshedCookies = authService.refresh(request);
        assertNotNull(refreshedCookies);
        assertFalse(refreshedCookies.isEmpty());
    }

    @Test
    @DisplayName("Refresh Token User Not Found Test")
    void refreshTokenUserNotFoundTest() {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        "superSecretPassword"
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");
        List<ResponseCookie> cookies = authService.authenticate(authentication, request);

        ResponseCookie jwtCookie = cookies.stream().filter(cookie -> cookie.getName().equals("TEAMWORK-ACCESS"))
                .findFirst().orElseThrow();

        ResponseCookie refreshCookie = cookies.stream().filter(cookie -> cookie.getName().equals("TEAMWORK-REFRESH"))
                .findFirst().orElseThrow();

        String jwtToken = jwtCookie.getValue();
        String refreshToken = refreshCookie.getValue();
        UUID userId = UUID.randomUUID();

        UUID sessionId = UUID.fromString((String) Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().get("sid"));

        List<String> roles = List.of((String) Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().get("sid"));

        Authentication jwtAuthentication = new UsernamePasswordAuthenticationToken(
                new JwtPrincipal(userId, sessionId, roles),
                null,
                List.of()
        );

        assertThrows(RuntimeException.class,
                () -> authService.refresh(request));
    }

    @Test
    @DisplayName("Refresh Token Session Not Found Test")
    void refreshTokenSessionNotFoundTest() {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        "superSecretPassword"
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");
        List<ResponseCookie> cookies = authService.authenticate(authentication, request);

        ResponseCookie jwtCookie = cookies.stream().filter(cookie -> cookie.getName().equals("TEAMWORK-ACCESS"))
                .findFirst().orElseThrow();

        ResponseCookie refreshCookie = cookies.stream().filter(cookie -> cookie.getName().equals("TEAMWORK-REFRESH"))
                .findFirst().orElseThrow();

        String jwtToken = jwtCookie.getValue();
        String refreshToken = refreshCookie.getValue();
        UUID userId = UUID.fromString(Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().getSubject());

        UUID sessionId = UUID.randomUUID();

        List<String> roles = List.of((String) Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().get("sid"));

        Authentication jwtAuthentication = new UsernamePasswordAuthenticationToken(
                new JwtPrincipal(userId, sessionId, roles),
                null,
                List.of()
        );

        assertThrows(RuntimeException.class,
                () -> authService.refresh(request));
    }

    @Test
    @DisplayName("Refresh Token Invalid Refresh Token Id Test")
    void refreshTokenInvalidRefreshTokenIdTest() {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        "superSecretPassword"
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");
        List<ResponseCookie> cookies = authService.authenticate(authentication, request);

        ResponseCookie jwtCookie = cookies.stream().filter(cookie -> cookie.getName().equals("TEAMWORK-ACCESS"))
                .findFirst().orElseThrow();

        ResponseCookie refreshCookie = cookies.stream().filter(cookie -> cookie.getName().equals("TEAMWORK-REFRESH"))
                .findFirst().orElseThrow();

        String jwtToken = jwtCookie.getValue();
        String refreshToken = refreshCookie.getValue();

        String newRefreshToken = new Random().nextLong() + refreshToken.substring(refreshToken.indexOf("."));
        UUID userId = UUID.fromString(Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().getSubject());

        UUID sessionId = UUID.fromString((String) Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().get("sid"));

        List<String> roles = List.of((String) Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().get("sid"));

        Authentication jwtAuthentication = new UsernamePasswordAuthenticationToken(
                new JwtPrincipal(userId, sessionId, roles),
                null,
                List.of()
        );

        assertThrows(RuntimeException.class,
                () -> authService.refresh(request));
    }

    @Test
    @DisplayName("Refresh Token Invalid Refresh Token Test")
    void refreshTokenInvalidRefreshTokenTest() {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        "superSecretPassword"
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");
        List<ResponseCookie> cookies = authService.authenticate(authentication, request);

        ResponseCookie jwtCookie = cookies.stream().filter(cookie -> cookie.getName().equals("TEAMWORK-ACCESS"))
                .findFirst().orElseThrow();

        ResponseCookie refreshCookie = cookies.stream().filter(cookie -> cookie.getName().equals("TEAMWORK-REFRESH"))
                .findFirst().orElseThrow();

        String jwtToken = jwtCookie.getValue();
        String refreshToken = refreshCookie.getValue();

        String newRefreshToken = refreshToken.substring(0, refreshToken.indexOf(".") + 1) + rawTokenService.generateOpaqueToken();
        UUID userId = UUID.fromString(Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().getSubject());

        UUID sessionId = UUID.fromString((String) Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().get("sid"));

        List<String> roles = List.of((String) Jwts.parser()
                .verifyWith((SecretKey) jwtUtils.key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().get("sid"));

        Authentication jwtAuthentication = new UsernamePasswordAuthenticationToken(
                new JwtPrincipal(userId, sessionId, roles),
                null,
                List.of()
        );

        assertThrows(RuntimeException.class,
                () -> authService.refresh(request));
    }
}
