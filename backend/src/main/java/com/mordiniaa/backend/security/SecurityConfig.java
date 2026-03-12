package com.mordiniaa.backend.security;

import com.mordiniaa.backend.security.exceptions.AccessDeniedExceptionHandler;
import com.mordiniaa.backend.security.exceptions.JwtAuthEntryPoint;
import com.mordiniaa.backend.security.filters.AuditLoggingFilter;
import com.mordiniaa.backend.security.filters.IpBlockFilter;
import com.mordiniaa.backend.security.filters.JwtAuthenticationFilter;
import com.mordiniaa.backend.security.filters.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true
)
public class SecurityConfig {

    @Bean
    public SecurityContextHolderStrategy securityContextHolderStrategy() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        return SecurityContextHolder.getContextHolderStrategy();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "PUT", "POST", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, AuditLoggingFilter auditLoggingFilter, RateLimitFilter rateLimitFilter, IpBlockFilter ipBlockFilter, JwtAuthEntryPoint jwtAuthEntryPoint, AccessDeniedExceptionHandler accessDeniedExceptionHandler) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .csrf(csrf ->
                        csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                .ignoringRequestMatchers("/sockjs/**")
                                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(config -> config
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                        .accessDeniedHandler(accessDeniedExceptionHandler)
                )
                .authorizeHttpRequests(requests ->
                        requests
                                .requestMatchers("/api/csrf-token").permitAll()
                                .requestMatchers(
                                        "/api/v1/auth/user",
                                        "/api/v1/auth/signout",
                                        "/api/v1/user/image/**",
                                        "/api/v1/storage/resource/**",
                                        "/api/v1/notes/**"
                                ).hasAnyRole("ADMIN", "MANAGER", "USER")
                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/v1/manager/**").hasRole("MANAGER")
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/v1/auth/signin",
                                        "/api/v1/auth/refresh",
                                        "/api/v1/auth/forgot-password",
                                        "/api/v1/auth/reset-password"
                                ).permitAll()
                                .requestMatchers(HttpMethod.GET, "/images/**").permitAll()
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
