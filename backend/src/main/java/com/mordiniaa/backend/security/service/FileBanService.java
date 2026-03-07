package com.mordiniaa.backend.security.service;

import com.mordiniaa.backend.config.StorageProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class FileBanService {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final Set<String> pending = ConcurrentHashMap.newKeySet();

    private final StorageProperties storageProperties;

    public List<String> loadBannedIps() {
        Path filePath = Path.of(storageProperties.getBannedIps().getPath());
        try {
            if (Files.notExists(filePath)) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
                return List.of();
            }
            return Files.readAllLines(filePath, Charset.defaultCharset());
        } catch (IOException e) {
            log.error("Error While Reading Banned Ips: {}", e.getMessage());
            throw new RuntimeException();
        }
    }

    public void enqueuePermanentBan(String ip) {
        if (pending.add(ip))
            queue.offer(ip);
    }

    void removePending(String ip) {
        pending.remove(ip);
    }
}
