package com.instagram.backend.service;

import org.springframework.http.HttpStatus;
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

    public AuthService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
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
        user.setPassword(request.password());

        UserAccount saved = userAccountRepository.save(user);
        return new RegisterResponse(saved.getId(), saved.getFullName(), saved.getUsername(), saved.getEmail());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(identifier)
                .or(() -> userAccountRepository.findByUsernameIgnoreCase(identifier))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.getPassword().equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return new LoginResponse(user.getId(), user.getFullName(), user.getUsername(), user.getEmail());
    }
}
