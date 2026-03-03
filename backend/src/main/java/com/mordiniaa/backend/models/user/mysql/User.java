package com.mordiniaa.backend.models.user.mysql;

import com.mordiniaa.backend.models.BaseEntity;
import com.mordiniaa.backend.models.team.Team;
import com.mordiniaa.backend.models.user.DbUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "User")
@ToString(exclude = {"addresses", "teams", "ownedTeams", "contact"})
@Table(name = "users", indexes = @Index(name = "fx_user_username", columnList = "username", unique = true))
public class User extends BaseEntity implements DbUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "first_name", nullable = false, length = 20)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 20)
    private String lastName;

    @Column(name = "username", nullable = false, length = 20, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "image_key", nullable = false)
    private String imageKey = "defaultProfileImage";

    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;

    @Column(name = "account_non_expired")
    private boolean accountNonExpired = true;

    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired = true;

    @Column(name = "credentials_expiry_date")
    private LocalDate credentialsExpiryDate;

    @Column(name = "account_expiry_date")
    private LocalDate accountExpiryDate;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "role_id", referencedColumnName = "role_id", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Set<Address> addresses = new HashSet<>();

    @ManyToMany(mappedBy = "teamMembers", fetch = FetchType.LAZY)
    private Set<Team> teams = new HashSet<>();

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private Set<Team> ownedTeams = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST)
    private Contact contact;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void addAddress(Address address) {
        this.addresses.add(address);
    }

    public Set<Address> getAddresses() {
        return Collections.unmodifiableSet(addresses);
    }

    @PrePersist
    public void prePersist() {
        if (credentialsExpiryDate == null) {
            credentialsExpiryDate = LocalDate.now().plusYears(1);
        }

        if (accountExpiryDate == null) {
            accountExpiryDate = LocalDate.now().plusYears(1);
        }
    }
}
