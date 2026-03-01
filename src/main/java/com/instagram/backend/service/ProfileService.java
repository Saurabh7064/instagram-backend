package com.instagram.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.instagram.backend.dto.MeResponse;
import com.instagram.backend.dto.ProfilePageResponse;
import com.instagram.backend.repository.UserAccountRepository;

@Service
public class ProfileService {

    private final UserAccountRepository userAccountRepository;
    private final TokenService tokenService;
    private final PostService postService;

    public ProfileService(
            UserAccountRepository userAccountRepository,
            TokenService tokenService,
            PostService postService) {
        this.userAccountRepository = userAccountRepository;
        this.tokenService = tokenService;
        this.postService = postService;
    }

    public MeResponse me(String authorizationHeader) {
        var user = authenticatedUser(authorizationHeader);
        return new MeResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt());
    }

    public ProfilePageResponse myProfilePage(String authorizationHeader) {
        var user = authenticatedUser(authorizationHeader);

        return new ProfilePageResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getBio() == null ? "" : user.getBio(),
                postService.countPostsFor(user),
                18,
                12,
                user.getCreatedAt());
    }

    private com.instagram.backend.domain.UserAccount authenticatedUser(String authorizationHeader) {
        TokenService.AuthenticatedPrincipal principal = tokenService.parseAccessToken(authorizationHeader);
        return userAccountRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for token"));
    }
}
