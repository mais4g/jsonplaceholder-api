package com.example.jsonplaceholderapi.repository;

import com.example.jsonplaceholderapi.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    // Buscar álbuns por usuário
    List<Album> findByUserId(Long userId);
    Page<Album> findByUserId(Long userId, Pageable pageable);

    // Buscar álbuns por título
    List<Album> findByTitleContainingIgnoreCase(String title);

    // Contar álbuns por usuário
    long countByUserId(Long userId);
}
