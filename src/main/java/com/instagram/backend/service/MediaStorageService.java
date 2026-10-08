package com.instagram.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.instagram.backend.dto.MediaUploadResponse;
import com.instagram.backend.observability.InstagramMetrics;

@Service
public class MediaStorageService {

    private final Path storageDirectory;
    private final TokenService tokenService;
    private final InstagramMetrics instagramMetrics;

    public MediaStorageService(
            @Value("${app.media.storage-dir:${java.io.tmpdir}/instagram-backend-uploads}") String storageDirectory,
            TokenService tokenService,
            InstagramMetrics instagramMetrics) {
        this.storageDirectory = Path.of(storageDirectory);
        this.tokenService = tokenService;
        this.instagramMetrics = instagramMetrics;
    }

    public MediaUploadResponse store(String authorizationHeader, MultipartFile file) {
        tokenService.parseAccessToken(authorizationHeader);

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A media file is required");
        }

        try {
            Files.createDirectories(storageDirectory);
            String cleanedName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename());
            String extension = extractExtension(cleanedName);
            String storedFileName = UUID.randomUUID() + extension;
            Path target = storageDirectory.resolve(storedFileName).normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
            instagramMetrics.recordMediaUpload(file.getSize(), contentType);
            return new MediaUploadResponse(storedFileName, "/uploads/" + storedFileName, contentType);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store media", exception);
        }
    }

    public Resource load(String fileName) {
        Path file = storageDirectory.resolve(fileName).normalize();
        if (!file.startsWith(storageDirectory) || !Files.exists(file) || Files.isDirectory(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media file not found");
        }
        return new FileSystemResource(file);
    }

    public String detectContentType(String fileName) {
        try {
            Path file = storageDirectory.resolve(fileName).normalize();
            String contentType = Files.probeContentType(file);
            return contentType == null ? "application/octet-stream" : contentType;
        } catch (IOException exception) {
            return "application/octet-stream";
        }
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return ".bin";
        }
        String extension = fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
        return extension.length() > 10 ? ".bin" : extension;
    }
}
