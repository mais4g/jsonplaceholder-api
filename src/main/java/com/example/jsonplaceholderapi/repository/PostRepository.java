package com.example.jsonplaceholderapi.repository;

import com.example.jsonplaceholderapi.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Buscar posts por usuário
    List<Post> findByUserId(Long userId);
    Page<Post> findByUserId(Long userId, Pageable pageable);

    // Buscar posts por título (contém)
    List<Post> findByTitleContainingIgnoreCase(String title);

    // Buscar posts por conteúdo
    @Query("SELECT p FROM Post p WHERE LOWER(p.body) LIKE LOWER(CONCAT('%', :content, '%'))")
    List<Post> findByContentContaining(@Param("content") String content);

    // Posts mais recentes
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findLatestPosts(Pageable pageable);
}