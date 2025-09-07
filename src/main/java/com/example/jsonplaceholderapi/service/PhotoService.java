package com.example.jsonplaceholderapi.service;

import com.example.jsonplaceholderapi.entity.Album;
import com.example.jsonplaceholderapi.entity.Photo;
import com.example.jsonplaceholderapi.entity.User;
import com.example.jsonplaceholderapi.repository.AlbumRepository;
import com.example.jsonplaceholderapi.repository.PhotoRepository;
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
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private UserRepository userRepository;

    // Buscar todas as fotos
    public List<Photo> findAll() {
        return photoRepository.findAll();
    }

    // Buscar todas com paginação
    public Page<Photo> findAll(Pageable pageable) {
        return photoRepository.findAll(pageable);
    }

    // Buscar por ID
    public Optional<Photo> findById(Long id) {
        return photoRepository.findById(id);
    }

    // Buscar fotos por álbum
    public List<Photo> findByAlbumId(Long albumId) {
        return photoRepository.findByAlbumId(albumId);
    }

    // Buscar fotos por álbum com paginação
    public Page<Photo> findByAlbumId(Long albumId, Pageable pageable) {
        return photoRepository.findByAlbumId(albumId, pageable);
    }

    // Buscar fotos por usuário
    public List<Photo> findByUserId(Long userId) {
        return photoRepository.findByAlbumUserId(userId);
    }

    // Buscar por título
    public List<Photo> findByTitle(String title) {
        return photoRepository.findByTitleContainingIgnoreCase(title);
    }

    // Contar fotos por álbum
    public long countByAlbumId(Long albumId) {
        return photoRepository.countByAlbumId(albumId);
    }

    // Criar foto
    public Photo create(Photo photo) {
        // Validar se álbum existe
        if (photo.getAlbum() != null && photo.getAlbum().getId() != null) {
            Album album = albumRepository.findById(photo.getAlbum().getId())
                    .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + photo.getAlbum().getId()));
            photo.setAlbum(album);

            // Se usuário não foi definido, herdar do álbum
            if (photo.getUser() == null) {
                photo.setUser(album.getUser());
            }
        } else {
            throw new RuntimeException("ID do álbum é obrigatório");
        }

        // Validar usuário se fornecido
        if (photo.getUser() != null && photo.getUser().getId() != null) {
            User user = userRepository.findById(photo.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + photo.getUser().getId()));
            photo.setUser(user);
        }

        return photoRepository.save(photo);
    }

    // Criar foto para álbum específico
    public Photo createForAlbum(Long albumId, Photo photo) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + albumId));

        photo.setAlbum(album);
        photo.setUser(album.getUser()); // Herdar usuário do álbum

        return photoRepository.save(photo);
    }

    // Atualizar foto
    public Photo update(Long id, Photo photoDetails) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada com ID: " + id));

        // Atualizar campos
        photo.setTitle(photoDetails.getTitle());
        photo.setUrl(photoDetails.getUrl());
        photo.setThumbnailUrl(photoDetails.getThumbnailUrl());
        photo.setAltText(photoDetails.getAltText());

        // Atualizar álbum se fornecido
        if (photoDetails.getAlbum() != null && photoDetails.getAlbum().getId() != null) {
            Album album = albumRepository.findById(photoDetails.getAlbum().getId())
                    .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + photoDetails.getAlbum().getId()));
            photo.setAlbum(album);
        }

        // Atualizar usuário se fornecido
        if (photoDetails.getUser() != null && photoDetails.getUser().getId() != null) {
            User user = userRepository.findById(photoDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + photoDetails.getUser().getId()));
            photo.setUser(user);
        }

        return photoRepository.save(photo);
    }

    // Atualizar parcialmente
    public Photo partialUpdate(Long id, Photo photoDetails) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada com ID: " + id));

        // Atualizar apenas campos não nulos
        if (photoDetails.getTitle() != null) {
            photo.setTitle(photoDetails.getTitle());
        }
        if (photoDetails.getUrl() != null) {
            photo.setUrl(photoDetails.getUrl());
        }
        if (photoDetails.getThumbnailUrl() != null) {
            photo.setThumbnailUrl(photoDetails.getThumbnailUrl());
        }
        if (photoDetails.getAltText() != null) {
            photo.setAltText(photoDetails.getAltText());
        }
        if (photoDetails.getAlbum() != null && photoDetails.getAlbum().getId() != null) {
            Album album = albumRepository.findById(photoDetails.getAlbum().getId())
                    .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + photoDetails.getAlbum().getId()));
            photo.setAlbum(album);
        }
        if (photoDetails.getUser() != null && photoDetails.getUser().getId() != null) {
            User user = userRepository.findById(photoDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + photoDetails.getUser().getId()));
            photo.setUser(user);
        }

        return photoRepository.save(photo);
    }

    // Deletar foto
    public void delete(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada com ID: " + id));

        photoRepository.delete(photo);
    }

    // Verificar se foto existe
    public boolean existsById(Long id) {
        return photoRepository.existsById(id);
    }

    // Verificar se usuário pode editar foto
    public boolean canUserEditPhoto(Long photoId, Long userId) {
        Optional<Photo> photo = photoRepository.findById(photoId);
        return photo.isPresent() && photo.get().getUser().getId().equals(userId);
    }
}