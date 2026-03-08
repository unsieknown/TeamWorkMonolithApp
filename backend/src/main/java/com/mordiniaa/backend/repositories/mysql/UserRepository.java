package com.mordiniaa.backend.repositories.mysql;

import com.mordiniaa.backend.models.user.mysql.AppRole;
import com.mordiniaa.backend.models.user.mysql.User;
import com.mordiniaa.backend.security.service.user.SecurityUserProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Modifying
    @Query("update User u set u.imageKey = :imageKey where u.userId = :userId")
    void updateImageKeyByUserId(String imageKey, UUID userId);

    boolean existsUserByFirstNameAndLastName(String firstName, String lastName);

    boolean existsByUsername(String username);

    @Modifying
    @Query("update User u set u.deleted = :deleted where u.userId = :userId")
    void updateDeletedByUserId(boolean deleted, UUID userId);

    @Query("select count(*) from User u where u.role.appRole = :appRole")
    int countByRole_AppRole(AppRole appRole);

    Optional<User> findUsersByRole_AppRole(AppRole roleAppRole);

    @Query("""
            select u.userId as userId,
            u.username as username,
            u.password as password,
            u.role as role,
            u.accountNonExpired as accountNonExpired,
            u.accountNonLocked as accountNonLocked,
            u.credentialsNonExpired as credentialsNonExpired,
            u.deleted as deleted
            from User u
            where u.username = :username
            """)
    Optional<SecurityUserProjection> findSecurityUserByUsername(String username);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update User u
            set u.password = :newPassword
            where u.userId = :userId
            """)
    void updatePasswordByUserId(UUID userId, String newPassword);

    Optional<User> findUserByRole_AppRole(AppRole roleAppRole);

    Optional<User> findUserByUserIdAndDeletedFalse(UUID userId);

    Optional<User> findUserByUserIdAndDeletedFalseAndRole_AppRole(UUID userId, AppRole roleAppRole);
}
