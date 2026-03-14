package com.mordiniaa.backend.security.service.user;

import com.mordiniaa.backend.repositories.mysql.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        SecurityUserProjection user = userRepository.findSecurityUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return SecurityUser.build(user);
    }
}
