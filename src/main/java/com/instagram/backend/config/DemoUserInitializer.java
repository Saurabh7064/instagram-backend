package com.instagram.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.instagram.backend.domain.UserAccount;
import com.instagram.backend.repository.UserAccountRepository;

@Component
public class DemoUserInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;

    public DemoUserInitializer(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public void run(String... args) {
        if (userAccountRepository.existsByUsernameIgnoreCase("demo.user")) {
            return;
        }

        UserAccount user = new UserAccount();
        user.setFullName("Demo User");
        user.setUsername("demo.user");
        user.setEmail("demo.user@example.com");
        user.setPassword("password123");
        userAccountRepository.save(user);
    }
}
