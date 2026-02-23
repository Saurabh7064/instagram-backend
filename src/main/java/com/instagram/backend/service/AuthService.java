package com.instagram.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.instagram.backend.domain.UserAccount;
import com.instagram.backend.dto.LoginRequest;
import com.instagram.backend.dto.LoginResponse;
import com.instagram.backend.dto.RegisterRequest;
import com.instagram.backend.dto.RegisterResponse;
import com.instagram.backend.repository.UserAccountRepository;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (userAccountRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        UserAccount user = new UserAccount();
        user.setFullName(request.fullName().trim());
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));

        UserAccount saved = userAccountRepository.save(user);
        TokenService.AuthToken token = tokenService.createAccessToken(saved);
        return new RegisterResponse(
                saved.getId(),
                saved.getFullName(),
                saved.getUsername(),
                saved.getEmail(),
                token.accessToken(),
                token.tokenType(),
                token.expiresAt());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(identifier)
                .or(() -> userAccountRepository.findByUsernameIgnoreCase(identifier))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordMatchesAndMigrateIfNeeded(user, request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        TokenService.AuthToken token = tokenService.createAccessToken(user);
        return new LoginResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                token.accessToken(),
                token.tokenType(),
                token.expiresAt());
    }

    private boolean passwordMatchesAndMigrateIfNeeded(UserAccount user, String rawPassword) {
        String storedPassword = user.getPassword();

        if (looksLikeBcrypt(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (!storedPassword.equals(rawPassword)) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        userAccountRepository.save(user);
        return true;
    }

    private boolean looksLikeBcrypt(String value) {
        return value != null && value.startsWith("$2");
    }
}
