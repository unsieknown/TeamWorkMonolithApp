package com.mordiniaa.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.app.jwt")
public class JwtProperties {

    private String tokenName;
    private String issuer;
    private String audience;
    private String secret;
    private int minutesOfLife;
    private List<String> whiteList;
}
