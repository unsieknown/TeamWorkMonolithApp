package com.mordiniaa.backend.security.model;

import com.mordiniaa.backend.models.Session;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "RefreshTokenFamily")
@Table(name = "refresh_token_families", indexes = {
        @Index(name = "idx_rtf_user", columnList = "user_id"),
        @Index(name = "idx_rtf_session", columnList = "session_id")
})
public class RefreshTokenFamily {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", updatable = false)
    private UUID userId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @OneToMany(mappedBy = "refreshTokenFamily", fetch = FetchType.LAZY)
    private List<RefreshTokenEntity> refreshTokens = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "session_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_family_session")
    )
    private Session session;

    public RefreshTokenFamily(UUID userId) {
        this.userId = userId;
    }
}
