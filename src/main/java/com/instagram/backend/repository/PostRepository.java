package com.instagram.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.instagram.backend.domain.Post;
import com.instagram.backend.domain.UserAccount;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByCreatedAtDesc();

    long countByAuthor(UserAccount author);
}
