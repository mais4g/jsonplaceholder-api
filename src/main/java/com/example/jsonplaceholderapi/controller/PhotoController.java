package com.example.jsonplaceholderapi.controller;

import com.example.jsonplaceholderapi.dto.ApiResponse;
import com.example.jsonplaceholderapi.entity.Photo;
import com.example.jsonplaceholderapi.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/photos")
@Tag(name = "Photos", description = "Operações CRUD para fotos")
@SecurityRequirement(name = "Bearer Authentication")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @GetMapping
    @Operation(summary = "Listar todas as fotos", description = "Retorna lista paginada de fotos")
    public ResponseEntity<Page<Photo>> getAllPhotos(
            @Parameter(description = "Número da página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Photo> photos = photoService.findAll(pageable);

            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas as fotos (sem paginação)", description = "Retorna lista completa de fotos")
    public ResponseEntity<List<Photo>> getAllPhotosNoPagination() {
        try {
            List<Photo> photos = photoService.findAll();
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar foto por ID", description = "Retorna uma foto específica pelo ID")
    public ResponseEntity<Photo> getPhotoById(
            @Parameter(description = "ID da foto")
            @PathVariable Long id) {

        try {
            Optional<Photo> photo = photoService.findById(id);
            return photo.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/album/{albumId}")
    @Operation(summary = "Buscar fotos por álbum", description = "Retorna fotos de um álbum específico")
    public ResponseEntity<Page<Photo>> getPhotosByAlbum(
            @Parameter(description = "ID do álbum")
            @PathVariable Long albumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Photo> photos = photoService.findByAlbumId(albumId, pageable);

            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Buscar fotos por usuário", description = "Retorna fotos de um usuário específico")
    public ResponseEntity<List<Photo>> getPhotosByUser(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId) {

        try {
            List<Photo> photos = photoService.findByUserId(userId);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/album/{albumId}/count")
    @Operation(summary = "Contar fotos do álbum", description = "Retorna o número de fotos de um álbum")
    public ResponseEntity<Long> countPhotosByAlbum(
            @Parameter(description = "ID do álbum")
            @PathVariable Long albumId) {

        try {
            long count = photoService.countByAlbumId(albumId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Criar nova foto", description = "Cria uma nova foto no sistema")
    public ResponseEntity<?> createPhoto(@Valid @RequestBody Photo photo) {
        try {
            Photo createdPhoto = photoService.create(photo);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPhoto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PostMapping("/album/{albumId}")
    @Operation(summary = "Criar foto para álbum", description = "Cria uma nova foto para um álbum específico")
    public ResponseEntity<?> createPhotoForAlbum(
            @Parameter(description = "ID do álbum")
            @PathVariable Long albumId,
            @Valid @RequestBody Photo photo) {

        try {
            Photo createdPhoto = photoService.createForAlbum(albumId, photo);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPhoto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar foto", description = "Atualiza todos os campos de uma foto")
    public ResponseEntity<?> updatePhoto(
            @Parameter(description = "ID da foto")
            @PathVariable Long id,
            @Valid @RequestBody Photo photoDetails) {

        try {
            Photo updatedPhoto = photoService.update(id, photoDetails);
            return ResponseEntity.ok(updatedPhoto);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar foto parcialmente", description = "Atualiza apenas os campos fornecidos")
    public ResponseEntity<?> partialUpdatePhoto(
            @Parameter(description = "ID da foto")
            @PathVariable Long id,
            @RequestBody Photo photoDetails) {

        try {
            Photo updatedPhoto = photoService.partialUpdate(id, photoDetails);
            return ResponseEntity.ok(updatedPhoto);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar foto", description = "Remove uma foto do sistema")
    public ResponseEntity<?> deletePhoto(
            @Parameter(description = "ID da foto")
            @PathVariable Long id) {

        try {
            photoService.delete(id);
            return ResponseEntity.ok(new ApiResponse(true, "Foto deletada com sucesso"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrada")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar fotos", description = "Busca fotos por título")
    public ResponseEntity<?> searchPhotos(
            @RequestParam(required = false) String title) {

        try {
            if (title != null) {
                List<Photo> photos = photoService.findByTitle(title);
                return ResponseEntity.ok(photos);
            }

            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Parâmetro 'title' é necessário"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar se foto existe", description = "Verifica se uma foto existe pelo ID")
    public ResponseEntity<ApiResponse> photoExists(
            @Parameter(description = "ID da foto")
            @PathVariable Long id) {

        try {
            boolean exists = photoService.existsById(id);
            return ResponseEntity.ok(new ApiResponse(exists,
                    exists ? "Foto existe" : "Foto não encontrada"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }
}