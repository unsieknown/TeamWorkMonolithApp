package com.mordiniaa.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private ProfileImages profileImages;
    private CloudStorage cloudStorage;
    private BannedIps bannedIps;

    @Getter
    @Setter
    public static class ProfileImages {
        private String path;
        private List<String> mimeTypes;
        private int profileWidth;
        private int profileHeight;
        private String defaultImageKey;
        private String defaultImagePath;
    }

    @Getter
    @Setter
    public static class CloudStorage {
        private String path;
    }

    @Getter
    @Setter
    public static class BannedIps {
        private String path;
    }
}
