package com.instagram.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.instagram.backend.dto.MeResponse;
import com.instagram.backend.service.ProfileService;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public MeResponse me(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return profileService.me(authorizationHeader);
    }
}
