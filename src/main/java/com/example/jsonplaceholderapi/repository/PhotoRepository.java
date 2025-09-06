package com.example.jsonplaceholderapi.repository;

import com.example.jsonplaceholderapi.entity.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    // Buscar fotos por álbum
    List<Photo> findByAlbumId(Long albumId);
    Page<Photo> findByAlbumId(Long albumId, Pageable pageable);

    // Buscar fotos por título
    List<Photo> findByTitleContainingIgnoreCase(String title);

    // Contar fotos por álbum
    long countByAlbumId(Long albumId);

    // Buscar fotos por usuário (através do álbum)
    List<Photo> findByAlbumUserId(Long userId);
}