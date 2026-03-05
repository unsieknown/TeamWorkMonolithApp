package com.mordiniaa.backend.security.service.token;

import com.mordiniaa.backend.models.Session;
import com.mordiniaa.backend.repositories.mysql.RefreshTokenFamilyRepository;
import com.mordiniaa.backend.security.model.RefreshTokenFamily;
import com.mordiniaa.backend.services.auth.SessionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RefreshTokenFamilyServiceTest {

    @Autowired
    private RefreshTokenFamilyService refreshTokenFamilyService;

    @Autowired
    private RefreshTokenFamilyRepository refreshTokenFamilyRepository;
    @Autowired
    private SessionService sessionService;

    @AfterEach
    void tearDown() {
        refreshTokenFamilyRepository.deleteAll();
    }

    @Test
    @DisplayName("Get Refresh Token Family")
    void getRefreshTokenFamilyOrCreateTest() {

        Long familyId = new Random().nextLong();
        UUID userId = UUID.randomUUID();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.123.123.123");
        request.addHeader("User-Agent", "Mozilla");
        Session session = sessionService.createSession(request);
        RefreshTokenFamily newFamily = refreshTokenFamilyService.createNewFamily(userId, session);
        assertNotNull(newFamily);

        assertEquals(userId, newFamily.getUserId());

        Long savedFamilyId = newFamily.getId();
        assertTrue(savedFamilyId > 1);
        assertNotEquals(familyId, savedFamilyId);
    }
}

