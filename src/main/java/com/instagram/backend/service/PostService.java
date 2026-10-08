package com.instagram.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.instagram.backend.domain.Post;
import com.instagram.backend.domain.PostLike;
import com.instagram.backend.dto.CreatePostRequest;
import com.instagram.backend.dto.FeedPostResponse;
import com.instagram.backend.dto.UpdatePostRequest;
import com.instagram.backend.repository.PostLikeRepository;
import com.instagram.backend.repository.PostRepository;
import com.instagram.backend.repository.UserAccountRepository;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

@Service
public class PostService {

    private static final String USER_NOT_FOUND = "User not found for token";

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserAccountRepository userAccountRepository;
    private final TokenService tokenService;
    private final ObservationRegistry observationRegistry;

    public PostService(
            PostLikeRepository postLikeRepository,
            PostRepository postRepository,
            UserAccountRepository userAccountRepository,
            TokenService tokenService,
            ObservationRegistry observationRegistry) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
        this.userAccountRepository = userAccountRepository;
        this.tokenService = tokenService;
        this.observationRegistry = observationRegistry;
    }

    @Transactional(readOnly = true)
    public List<FeedPostResponse> feed(String authorizationHeader) {
        return Observation.createNotStarted("instagram.feed.load", observationRegistry)
                .contextualName("load-feed")
                .lowCardinalityKeyValue("operation", "feed")
                .observe(() -> {
                    var viewer = requireViewer(authorizationHeader);
                    return postRepository.findAllByOrderByCreatedAtDesc().stream()
                            .map(post -> toResponse(post, viewer))
                            .toList();
                });
    }

    @Transactional
    public FeedPostResponse create(String authorizationHeader, CreatePostRequest request) {
        var author = requireViewer(authorizationHeader);

        Post post = new Post();
        post.setAuthor(author);
        post.setCaption(request.caption().trim());
        post.setImageUrl(request.imageUrl().trim());
        post.setLocationLabel(request.locationLabel().trim());
        post.setLikeCount(0);

        return toResponse(postRepository.save(post), author);
    }

    @Transactional
    public FeedPostResponse like(String authorizationHeader, Long postId) {
        var viewer = requireViewer(authorizationHeader);
        var post = requirePost(postId);

        if (!postLikeRepository.existsByPostAndUser(post, viewer)) {
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(viewer);
            postLikeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
        }

        return toResponse(postRepository.save(post), viewer);
    }

    @Transactional
    public FeedPostResponse unlike(String authorizationHeader, Long postId) {
        var viewer = requireViewer(authorizationHeader);
        var post = requirePost(postId);

        postLikeRepository.findByPostAndUser(post, viewer).ifPresent(existing -> {
            postLikeRepository.delete(existing);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        });

        return toResponse(postRepository.save(post), viewer);
    }

    @Transactional
    public FeedPostResponse update(String authorizationHeader, Long postId, UpdatePostRequest request) {
        var viewer = requireViewer(authorizationHeader);
        var post = requireOwnedPost(viewer, postId);

        post.setCaption(request.caption().trim());
        post.setLocationLabel(request.locationLabel().trim());

        return toResponse(postRepository.save(post), viewer);
    }

    @Transactional
    public void delete(String authorizationHeader, Long postId) {
        var viewer = requireViewer(authorizationHeader);
        var post = requireOwnedPost(viewer, postId);

        postLikeRepository.deleteAllByPost(post);
        postRepository.delete(post);
    }

    public long countPostsFor(com.instagram.backend.domain.UserAccount user) {
        return postRepository.countByAuthor(user);
    }

    private com.instagram.backend.domain.UserAccount requireViewer(String authorizationHeader) {
        TokenService.AuthenticatedPrincipal principal = tokenService.parseAccessToken(authorizationHeader);
        return userAccountRepository.findById(principal.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));
    }

    private Post requirePost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private Post requireOwnedPost(com.instagram.backend.domain.UserAccount viewer, Long postId) {
        var post = requirePost(postId);
        if (!post.getAuthor().getId().equals(viewer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own posts");
        }
        return post;
    }

    private FeedPostResponse toResponse(Post post, com.instagram.backend.domain.UserAccount viewer) {
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
                postLikeRepository.existsByPostAndUser(post, viewer),
                post.getCreatedAt());
    }
}
