package com.instagram.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.instagram.backend.domain.Post;
import com.instagram.backend.domain.PostLike;
import com.instagram.backend.domain.UserAccount;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndUser(Post post, UserAccount user);

    void deleteAllByPost(Post post);

    Optional<PostLike> findByPostAndUser(Post post, UserAccount user);
}
