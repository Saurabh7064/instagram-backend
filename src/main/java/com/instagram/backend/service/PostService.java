package com.instagram.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.instagram.backend.domain.Post;
import com.instagram.backend.dto.CreatePostRequest;
import com.instagram.backend.dto.FeedPostResponse;
import com.instagram.backend.repository.PostRepository;
import com.instagram.backend.repository.UserAccountRepository;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserAccountRepository userAccountRepository;
    private final TokenService tokenService;

    public PostService(
            PostRepository postRepository,
            UserAccountRepository userAccountRepository,
            TokenService tokenService) {
        this.postRepository = postRepository;
        this.userAccountRepository = userAccountRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public List<FeedPostResponse> feed(String authorizationHeader) {
        tokenService.parseAccessToken(authorizationHeader);
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FeedPostResponse create(String authorizationHeader, CreatePostRequest request) {
        TokenService.AuthenticatedPrincipal principal = tokenService.parseAccessToken(authorizationHeader);
        var author = userAccountRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for token"));

        Post post = new Post();
        post.setAuthor(author);
        post.setCaption(request.caption().trim());
        post.setImageUrl(request.imageUrl().trim());
        post.setLocationLabel(request.locationLabel().trim());
        post.setLikeCount(0);

        return toResponse(postRepository.save(post));
    }

    public long countPostsFor(com.instagram.backend.domain.UserAccount user) {
        return postRepository.countByAuthor(user);
    }

    private FeedPostResponse toResponse(Post post) {
        String avatarUrl = "/mock/avatar-generic.svg";
        if ("a".equalsIgnoreCase(post.getAuthor().getUsername())) {
            avatarUrl = "/mock/avatar-a.svg";
        } else if ("mira.frames".equalsIgnoreCase(post.getAuthor().getUsername())) {
            avatarUrl = "/mock/avatar-mira.svg";
        }

        return new FeedPostResponse(
                post.getId(),
                post.getAuthor().getUsername(),
                post.getAuthor().getFullName(),
                avatarUrl,
                post.getCaption(),
                post.getImageUrl(),
                post.getLocationLabel(),
                post.getLikeCount(),
                post.getCreatedAt());
    }
}
