package com.instagram.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.instagram.backend.domain.UserAccount;
import com.instagram.backend.repository.UserAccountRepository;

@Component
public class DemoUserInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUserInitializer(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        var existing = userAccountRepository.findByUsernameIgnoreCase("demo.user");
        if (existing.isPresent()) {
            UserAccount user = existing.get();
            if (!looksLikeBcrypt(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userAccountRepository.save(user);
            }
            return;
        }

        UserAccount user = new UserAccount();
        user.setFullName("Demo User");
        user.setUsername("demo.user");
        user.setEmail("demo.user@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userAccountRepository.save(user);
    }

    private boolean looksLikeBcrypt(String value) {
        return value != null && value.startsWith("$2");
    }
}
