package com.example.jsonplaceholderapi.controller;

import com.example.jsonplaceholderapi.dto.ApiResponse;
import com.example.jsonplaceholderapi.entity.Album;
import com.example.jsonplaceholderapi.service.AlbumService;
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
@RequestMapping("/albums")
@Tag(name = "Albums", description = "Operações CRUD para álbuns")
@SecurityRequirement(name = "Bearer Authentication")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @GetMapping
    @Operation(summary = "Listar todos os álbuns", description = "Retorna lista paginada de álbuns")
    public ResponseEntity<Page<Album>> getAllAlbums(
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
            Page<Album> albums = albumService.findAll(pageable);

            return ResponseEntity.ok(albums);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos os álbuns (sem paginação)", description = "Retorna lista completa de álbuns")
    public ResponseEntity<List<Album>> getAllAlbumsNoPagination() {
        try {
            List<Album> albums = albumService.findAll();
            return ResponseEntity.ok(albums);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar álbum por ID", description = "Retorna um álbum específico pelo ID")
    public ResponseEntity<Album> getAlbumById(
            @Parameter(description = "ID do álbum")
            @PathVariable Long id) {

        try {
            Optional<Album> album = albumService.findById(id);
            return album.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Buscar álbuns por usuário", description = "Retorna álbuns de um usuário específico")
    public ResponseEntity<Page<Album>> getAlbumsByUser(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Album> albums = albumService.findByUserId(userId, pageable);

            return ResponseEntity.ok(albums);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Contar álbuns do usuário", description = "Retorna o número de álbuns de um usuário")
    public ResponseEntity<Long> countAlbumsByUser(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId) {

        try {
            long count = albumService.countByUserId(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Criar novo álbum", description = "Cria um novo álbum no sistema")
    public ResponseEntity<?> createAlbum(@Valid @RequestBody Album album) {
        try {
            Album createdAlbum = albumService.create(album);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAlbum);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PostMapping("/user/{userId}")
    @Operation(summary = "Criar álbum para usuário", description = "Cria um novo álbum para um usuário específico")
    public ResponseEntity<?> createAlbumForUser(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId,
            @Valid @RequestBody Album album) {

        try {
            Album createdAlbum = albumService.createForUser(userId, album);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAlbum);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar álbum", description = "Atualiza todos os campos de um álbum")
    public ResponseEntity<?> updateAlbum(
            @Parameter(description = "ID do álbum")
            @PathVariable Long id,
            @Valid @RequestBody Album albumDetails) {

        try {
            Album updatedAlbum = albumService.update(id, albumDetails);
            return ResponseEntity.ok(updatedAlbum);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
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
    @Operation(summary = "Atualizar álbum parcialmente", description = "Atualiza apenas os campos fornecidos")
    public ResponseEntity<?> partialUpdateAlbum(
            @Parameter(description = "ID do álbum")
            @PathVariable Long id,
            @RequestBody Album albumDetails) {

        try {
            Album updatedAlbum = albumService.partialUpdate(id, albumDetails);
            return ResponseEntity.ok(updatedAlbum);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
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
    @Operation(summary = "Deletar álbum", description = "Remove um álbum do sistema")
    public ResponseEntity<?> deleteAlbum(
            @Parameter(description = "ID do álbum")
            @PathVariable Long id) {

        try {
            albumService.delete(id);
            return ResponseEntity.ok(new ApiResponse(true, "Álbum deletado com sucesso"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("não encontrado")) {
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
    @Operation(summary = "Buscar álbuns", description = "Busca álbuns por título")
    public ResponseEntity<?> searchAlbums(
            @RequestParam(required = false) String title) {

        try {
            if (title != null) {
                List<Album> albums = albumService.findByTitle(title);
                return ResponseEntity.ok(albums);
            }

            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Parâmetro 'title' é necessário"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar se álbum existe", description = "Verifica se um álbum existe pelo ID")
    public ResponseEntity<ApiResponse> albumExists(
            @Parameter(description = "ID do álbum")
            @PathVariable Long id) {

        try {
            boolean exists = albumService.existsById(id);
            return ResponseEntity.ok(new ApiResponse(exists,
                    exists ? "Álbum existe" : "Álbum não encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }
}