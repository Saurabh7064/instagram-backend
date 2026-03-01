package com.instagram.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.instagram.backend.domain.Post;
import com.instagram.backend.domain.UserAccount;
import com.instagram.backend.repository.PostRepository;
import com.instagram.backend.repository.UserAccountRepository;

@Component
public class DemoUserInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUserInitializer(
            UserAccountRepository userAccountRepository,
            PostRepository postRepository,
            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String demoBio = "Building Instagram clone to learn distributed systems.";
        upsertDemoUser("a", "A", "a@example.com", "a", demoBio);
        upsertDemoUser(
                "mira.frames",
                "Mira Frames",
                "mira.frames@example.com",
                "a",
                "Shoots city light, geometry, and slow mornings.");

        if (postRepository.count() == 0) {
            seedPost("mira.frames", "Shot this after sunrise. Strong light, clean shadows, no filters needed.");
            seedPost("a", "Building the home feed and tying the app to concrete learning goals.");
        }
    }

    private void upsertDemoUser(String username, String fullName, String email, String password, String bio) {
        var existing = userAccountRepository.findByUsernameIgnoreCase(username);
        if (existing.isPresent()) {
            UserAccount user = existing.get();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setBio(bio);
            userAccountRepository.save(user);
            return;
        }

        UserAccount user = new UserAccount();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setBio(bio);
        userAccountRepository.save(user);
    }

    private void seedPost(String username, String caption) {
        UserAccount author = userAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow();

        Post post = new Post();
        post.setAuthor(author);
        post.setCaption(caption);
        post.setImageUrl("/mock/post-canyon.svg");
        post.setLocationLabel("Home Feed Demo");
        post.setLikeCount("mira.frames".equals(username) ? 1284 : 128);
        postRepository.save(post);
    }
}
