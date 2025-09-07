package com.example.jsonplaceholderapi.controller;

import com.example.jsonplaceholderapi.dto.ApiResponse;
import com.example.jsonplaceholderapi.entity.Post;
import com.example.jsonplaceholderapi.service.PostService;
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
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Operações CRUD para posts")
@SecurityRequirement(name = "Bearer Authentication")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    @Operation(summary = "Listar todos os posts", description = "Retorna lista paginada de posts")
    public ResponseEntity<Page<Post>> getAllPosts(
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
            Page<Post> posts = postService.findAll(pageable);

            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos os posts (sem paginação)", description = "Retorna lista completa de posts")
    public ResponseEntity<List<Post>> getAllPostsNoPagination() {
        try {
            List<Post> posts = postService.findAll();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar post por ID", description = "Retorna um post específico pelo ID")
    public ResponseEntity<Post> getPostById(
            @Parameter(description = "ID do post")
            @PathVariable Long id) {

        try {
            Optional<Post> post = postService.findById(id);
            return post.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Buscar posts por usuário", description = "Retorna posts de um usuário específico")
    public ResponseEntity<Page<Post>> getPostsByUser(
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
            Page<Post> posts = postService.findByUserId(userId, pageable);

            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/latest")
    @Operation(summary = "Posts mais recentes", description = "Retorna os posts mais recentes")
    public ResponseEntity<Page<Post>> getLatestPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Post> posts = postService.findLatestPosts(pageable);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Criar novo post", description = "Cria um novo post no sistema")
    public ResponseEntity<?> createPost(@Valid @RequestBody Post post) {
        try {
            Post createdPost = postService.create(post);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PostMapping("/user/{userId}")
    @Operation(summary = "Criar post para usuário", description = "Cria um novo post para um usuário específico")
    public ResponseEntity<?> createPostForUser(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId,
            @Valid @RequestBody Post post) {

        try {
            Post createdPost = postService.create(post, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar post", description = "Atualiza todos os campos de um post")
    public ResponseEntity<?> updatePost(
            @Parameter(description = "ID do post")
            @PathVariable Long id,
            @Valid @RequestBody Post postDetails) {

        try {
            Post updatedPost = postService.update(id, postDetails);
            return ResponseEntity.ok(updatedPost);
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
    @Operation(summary = "Atualizar post parcialmente", description = "Atualiza apenas os campos fornecidos")
    public ResponseEntity<?> partialUpdatePost(
            @Parameter(description = "ID do post")
            @PathVariable Long id,
            @RequestBody Post postDetails) {

        try {
            Post updatedPost = postService.partialUpdate(id, postDetails);
            return ResponseEntity.ok(updatedPost);
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
    @Operation(summary = "Deletar post", description = "Remove um post do sistema")
    public ResponseEntity<?> deletePost(
            @Parameter(description = "ID do post")
            @PathVariable Long id) {

        try {
            postService.delete(id);
            return ResponseEntity.ok(new ApiResponse(true, "Post deletado com sucesso"));
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
    @Operation(summary = "Buscar posts", description = "Busca posts por título ou conteúdo")
    public ResponseEntity<?> searchPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content) {

        try {
            if (title != null) {
                List<Post> posts = postService.findByTitle(title);
                return ResponseEntity.ok(posts);
            }

            if (content != null) {
                List<Post> posts = postService.findByContent(content);
                return ResponseEntity.ok(posts);
            }

            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Pelo menos um parâmetro de busca é necessário"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar se post existe", description = "Verifica se um post existe pelo ID")
    public ResponseEntity<ApiResponse> postExists(
            @Parameter(description = "ID do post")
            @PathVariable Long id) {

        try {
            boolean exists = postService.existsById(id);
            return ResponseEntity.ok(new ApiResponse(exists,
                    exists ? "Post existe" : "Post não encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }
}