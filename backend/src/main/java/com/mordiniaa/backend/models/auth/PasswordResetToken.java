package com.mordiniaa.backend.models.auth;

import com.mordiniaa.backend.models.user.mysql.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Table(name = "password_reset_tokens")
@Entity(name = "PasswordResetToken")
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "token")
    private UUID token;

    @Column(nullable = false, name = "expiry_date")
    private Instant expiryDate;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public PasswordResetToken(UUID token, Instant expiryDate, User user) {
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
    }
}
