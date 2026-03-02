package com.instagram.backend.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.instagram.backend.dto.MediaUploadResponse;
import com.instagram.backend.service.MediaStorageService;

@RestController
@RequestMapping
public class MediaController {

    private final MediaStorageService mediaStorageService;

    public MediaController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    @PostMapping("/api/media")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaUploadResponse upload(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("file") MultipartFile file) {
        return mediaStorageService.store(authorizationHeader, file);
    }

    @GetMapping("/uploads/{fileName:.+}")
    public ResponseEntity<Resource> fetch(@PathVariable String fileName) {
        Resource resource = mediaStorageService.load(fileName);
        String contentType = mediaStorageService.detectContentType(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
