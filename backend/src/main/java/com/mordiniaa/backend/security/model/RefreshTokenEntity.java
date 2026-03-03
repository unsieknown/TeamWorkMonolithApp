package com.mordiniaa.backend.security.model;

import com.mordiniaa.backend.utils.JsonStringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "RefreshToken")
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_rt_family", columnList = "refresh_token_family"),
        @Index(name = "idx_rt_revoked", columnList = "revoked")
})
public class RefreshTokenEntity {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "refresh_token_family", nullable = false)
    private RefreshTokenFamily refreshTokenFamily;

    @Column(name = "hashed_token", updatable = false, nullable = false)
    private String hashedToken;

    @Builder.Default
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "replaced_by_id")
    private Long replacedById;

    @Convert(converter = JsonStringListConverter.class)
    @Column(columnDefinition = "json", nullable = false)
    private List<String> roles;
}
