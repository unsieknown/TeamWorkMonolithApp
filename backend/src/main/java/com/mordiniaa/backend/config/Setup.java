package com.mordiniaa.backend.config;

import com.mordiniaa.backend.models.user.mysql.AppRole;
import com.mordiniaa.backend.models.user.mysql.Role;
import com.mordiniaa.backend.models.user.mysql.User;
import com.mordiniaa.backend.repositories.mysql.RoleRepository;
import com.mordiniaa.backend.repositories.mysql.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
@Component
@RequiredArgsConstructor
public class Setup {

    @Bean
    @Profile("!test")
    @Transactional
    CommandLineRunner setAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String password = Files.readString(Path.of("/run/secrets/admin_password.txt")).trim();
            long adminCount = userRepository.countByRole_AppRole(AppRole.ROLE_ADMIN);
            if (adminCount != 1) {
                if (adminCount > 1) {
                    throw new RuntimeException("There Are More Admins");
                } else {
                    throw new RuntimeException("No Admin Set");
                }
            }

            User user = userRepository.findUsersByRole_AppRole(AppRole.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("Admin user missing"));

            if (user.getPassword() != null) {
                log.info("Admin Password Already Set");
                return;
            }

            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);

            log.info("Admin password initialized from environment");
        };
    }

    @Bean
    @Profile("test")
    @Transactional
    CommandLineRunner setAdminTestProfile(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        return args -> {
            String password = "superSecretPassword";
            long adminCount = userRepository.countByRole_AppRole(AppRole.ROLE_ADMIN);
            if (adminCount != 1) {
                if (adminCount > 1) {
                    throw new RuntimeException("There Are More Admins");
                }
            }

            User user = userRepository.findUsersByRole_AppRole(AppRole.ROLE_ADMIN)
                    .orElseGet(() -> {
                        Role adminRole = roleRepository.findRoleByAppRole(AppRole.ROLE_ADMIN)
                                .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_ADMIN)));
                        User admin = new User();
                        admin.setFirstName("Admin");
                        admin.setLastName("Admin");
                        admin.setUsername("admin");
                        admin.setRole(adminRole);
                        admin.setPassword(passwordEncoder.encode(password));
                        admin.setImageKey("Https://random.com");
                        return userRepository.save(admin);
                    });

            if (user.getPassword() != null) {
                log.info("Admin Password Already Set");
                return;
            }

            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);

            log.info("Admin password initialized from environment");
        };
    }
}
