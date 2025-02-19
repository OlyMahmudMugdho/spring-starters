package com.mahmud.basicfileupload.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.io.IOException;

@Service
public class TempFileCleanup {

    @Scheduled(fixedRate = 86400000) // Run daily
    public void cleanupTempFiles() {
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Files.walk(tempDir)
                    .filter(path -> path.startsWith("spring-upload-"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {}
                    });
        } catch (IOException e) {
            // Handle exception
        }
    }
}