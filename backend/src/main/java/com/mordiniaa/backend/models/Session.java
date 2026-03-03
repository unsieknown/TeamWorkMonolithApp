package com.mordiniaa.backend.models;

import com.mordiniaa.backend.models.user.mysql.User;
import com.mordiniaa.backend.security.model.RefreshTokenFamily;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "sessions")
@Entity(name = "Session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID sessionId;

    @Column(name = "user_agent", nullable = false)
    private String userAgent;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "last_activity")
    private Instant lastActivity;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @OneToOne(mappedBy = "session")
    private RefreshTokenFamily refreshTokenFamily;
}
