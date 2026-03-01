package com.instagram.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.instagram.backend.dto.CreatePostRequest;
import com.instagram.backend.dto.FeedPostResponse;
import com.instagram.backend.dto.UpdatePostRequest;
import com.instagram.backend.service.PostService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/feed")
    public List<FeedPostResponse> feed(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return postService.feed(authorizationHeader);
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public FeedPostResponse create(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody CreatePostRequest request) {
        return postService.create(authorizationHeader, request);
    }

    @PostMapping("/posts/{postId}/like")
    public FeedPostResponse like(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long postId) {
        return postService.like(authorizationHeader, postId);
    }

    @DeleteMapping("/posts/{postId}/like")
    public FeedPostResponse unlike(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long postId) {
        return postService.unlike(authorizationHeader, postId);
    }

    @PutMapping("/posts/{postId}")
    public FeedPostResponse update(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request) {
        return postService.update(authorizationHeader, postId, request);
    }

    @DeleteMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long postId) {
        postService.delete(authorizationHeader, postId);
    }
}
