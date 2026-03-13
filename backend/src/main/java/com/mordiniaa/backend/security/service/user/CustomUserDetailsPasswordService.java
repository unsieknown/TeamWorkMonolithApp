package com.mordiniaa.backend.security.service.user;

import com.mordiniaa.backend.exceptions.BadRequestException;
import com.mordiniaa.backend.repositories.mysql.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsPasswordService implements UserDetailsPasswordService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {

        SecurityUser securityUser = (SecurityUser) user;
        UUID userId = securityUser.getUserId();

        userRepository.updatePasswordByUserId(userId, newPassword);

        SecurityUserProjection updatedUser = userRepository.findSecurityUserByUsername(securityUser.getUsername())
                .orElseThrow(() -> new BadRequestException("User Not Found"));

        return SecurityUser.build(updatedUser);
    }
}
