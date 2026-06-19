package com.jobportal.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for file uploads.
 * Creates the upload directory on startup if it doesn't exist.
 */
@Configuration
public class FileUploadConfig {

    private static final Logger log = LoggerFactory.getLogger(FileUploadConfig.class);

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", uploadDir, e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String getUploadDir() {
        return uploadDir;
    }
}
