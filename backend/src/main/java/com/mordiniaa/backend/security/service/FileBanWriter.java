package com.mordiniaa.backend.security.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileBanWriter {

    private Path FILE;

    private final FileBanService fileBanService;

    @PostConstruct
    public void start() {
        FILE = Path.of(fileBanService.getStorageProperties().getBannedIps().getPath());
        log.info("Ban writer started, file={}", FILE);
        Thread.ofVirtual().start(this::process);
    }

    private void process() {

        BlockingQueue<String> queue = fileBanService.getQueue();
        List<String> batch = new ArrayList<>(100);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                String first = queue.take();
                batch.add(first);

                while (batch.size() < 100) {
                    String next = queue.poll(50, TimeUnit.MILLISECONDS);
                    if (next == null) break;

                    batch.add(next);
                }

                Files.write(
                        FILE,
                        batch,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );

                batch.forEach(fileBanService::removePending);
                batch.clear();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("An Error Occurred While Writing To Ban File: {}", e.getMessage());
            }
        }
    }
}
