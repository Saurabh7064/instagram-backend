package com.instagram.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.instagram.backend.dto.MeResponse;
import com.instagram.backend.repository.UserAccountRepository;

@Service
public class ProfileService {

    private final UserAccountRepository userAccountRepository;
    private final TokenService tokenService;

    public ProfileService(UserAccountRepository userAccountRepository, TokenService tokenService) {
        this.userAccountRepository = userAccountRepository;
        this.tokenService = tokenService;
    }

    public MeResponse me(String authorizationHeader) {
        TokenService.AuthenticatedPrincipal principal = tokenService.parseAccessToken(authorizationHeader);
        var user = userAccountRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for token"));

        return new MeResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt());
    }
}
