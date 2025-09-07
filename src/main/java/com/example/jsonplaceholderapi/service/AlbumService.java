package com.example.jsonplaceholderapi.service;

import com.example.jsonplaceholderapi.entity.Album;
import com.example.jsonplaceholderapi.entity.User;
import com.example.jsonplaceholderapi.repository.AlbumRepository;
import com.example.jsonplaceholderapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private UserRepository userRepository;

    // Buscar todos os álbuns
    public List<Album> findAll() {
        return albumRepository.findAll();
    }

    // Buscar todos com paginação
    public Page<Album> findAll(Pageable pageable) {
        return albumRepository.findAll(pageable);
    }

    // Buscar por ID
    public Optional<Album> findById(Long id) {
        return albumRepository.findById(id);
    }

    // Buscar álbuns por usuário
    public List<Album> findByUserId(Long userId) {
        return albumRepository.findByUserId(userId);
    }

    // Buscar álbuns por usuário com paginação
    public Page<Album> findByUserId(Long userId, Pageable pageable) {
        return albumRepository.findByUserId(userId, pageable);
    }

    // Buscar por título
    public List<Album> findByTitle(String title) {
        return albumRepository.findByTitleContainingIgnoreCase(title);
    }

    // Contar álbuns por usuário
    public long countByUserId(Long userId) {
        return albumRepository.countByUserId(userId);
    }

    // Criar álbum
    public Album create(Album album) {
        // Validar se usuário existe
        if (album.getUser() != null && album.getUser().getId() != null) {
            User user = userRepository.findById(album.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + album.getUser().getId()));
            album.setUser(user);
        } else {
            throw new RuntimeException("ID do usuário é obrigatório");
        }

        return albumRepository.save(album);
    }

    // Criar álbum para usuário específico
    public Album createForUser(Long userId, Album album) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));

        album.setUser(user);
        return albumRepository.save(album);
    }

    // Atualizar álbum
    public Album update(Long id, Album albumDetails) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + id));

        // Atualizar campos
        album.setTitle(albumDetails.getTitle());
        album.setDescription(albumDetails.getDescription());

        // Atualizar usuário se fornecido
        if (albumDetails.getUser() != null && albumDetails.getUser().getId() != null) {
            User user = userRepository.findById(albumDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + albumDetails.getUser().getId()));
            album.setUser(user);
        }

        return albumRepository.save(album);
    }

    // Atualizar parcialmente
    public Album partialUpdate(Long id, Album albumDetails) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + id));

        // Atualizar apenas campos não nulos
        if (albumDetails.getTitle() != null) {
            album.setTitle(albumDetails.getTitle());
        }
        if (albumDetails.getDescription() != null) {
            album.setDescription(albumDetails.getDescription());
        }
        if (albumDetails.getUser() != null && albumDetails.getUser().getId() != null) {
            User user = userRepository.findById(albumDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + albumDetails.getUser().getId()));
            album.setUser(user);
        }

        return albumRepository.save(album);
    }

    // Deletar álbum
    public void delete(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + id));

        albumRepository.delete(album);
    }

    // Verificar se álbum existe
    public boolean existsById(Long id) {
        return albumRepository.existsById(id);
    }

    // Verificar se usuário pode editar álbum
    public boolean canUserEditAlbum(Long albumId, Long userId) {
        Optional<Album> album = albumRepository.findById(albumId);
        return album.isPresent() && album.get().getUser().getId().equals(userId);
    }
}