package com.mordiniaa.backend.security;

import com.mordiniaa.backend.security.filters.AuditLoggingFilter;
import com.mordiniaa.backend.security.filters.IpBlockFilter;
import com.mordiniaa.backend.security.filters.JwtAuthenticationFilter;
import com.mordiniaa.backend.security.filters.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true
)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, AuditLoggingFilter auditLoggingFilter, RateLimitFilter rateLimitFilter, IpBlockFilter ipBlockFilter) throws Exception {
        return http
                .csrf(csrf ->
                        csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                .ignoringRequestMatchers("/sockjs/**")
                                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests ->
                        requests
                                .requestMatchers("/api/csrf-token").permitAll()
                                .requestMatchers(
                                        "/api/v1/auth/user",
                                        "/api/v1/auth/signout",
                                        "/api/v1/auth/refresh"
                                ).hasAnyRole("ADMIN", "MANAGER", "USER")
                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/v1/auth/signin").permitAll()
                                .requestMatchers("/api/v1/test/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(ipBlockFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(auditLoggingFilter, IpBlockFilter.class)
                .addFilterAfter(rateLimitFilter, AuditLoggingFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RateLimitFilter.class)
                .build();
    }
}
