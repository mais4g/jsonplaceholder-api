package com.example.jsonplaceholderapi.controller;

import com.example.jsonplaceholderapi.dto.ApiResponse;
import com.example.jsonplaceholderapi.entity.Comment;
import com.example.jsonplaceholderapi.service.CommentService;
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
@RequestMapping("/comments")
@Tag(name = "Comments", description = "Operações CRUD para comentários")
@SecurityRequirement(name = "Bearer Authentication")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    @Operation(summary = "Listar todos os comentários", description = "Retorna lista paginada de comentários")
    public ResponseEntity<Page<Comment>> getAllComments(
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
            Page<Comment> comments = commentService.findAll(pageable);

            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos os comentários (sem paginação)", description = "Retorna lista completa de comentários")
    public ResponseEntity<List<Comment>> getAllCommentsNoPagination() {
        try {
            List<Comment> comments = commentService.findAll();
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar comentário por ID", description = "Retorna um comentário específico pelo ID")
    public ResponseEntity<Comment> getCommentById(
            @Parameter(description = "ID do comentário")
            @PathVariable Long id) {

        try {
            Optional<Comment> comment = commentService.findById(id);
            return comment.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Buscar comentários por post", description = "Retorna comentários de um post específico")
    public ResponseEntity<Page<Comment>> getCommentsByPost(
            @Parameter(description = "ID do post")
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Comment> comments = commentService.findByPostId(postId, pageable);

            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/post/{postId}/count")
    @Operation(summary = "Contar comentários do post", description = "Retorna o número de comentários de um post")
    public ResponseEntity<Long> countCommentsByPost(
            @Parameter(description = "ID do post")
            @PathVariable Long postId) {

        try {
            long count = commentService.countByPostId(postId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Criar novo comentário", description = "Cria um novo comentário no sistema")
    public ResponseEntity<?> createComment(@Valid @RequestBody Comment comment) {
        try {
            Comment createdComment = commentService.create(comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PostMapping("/post/{postId}")
    @Operation(summary = "Criar comentário para post", description = "Cria um novo comentário para um post específico")
    public ResponseEntity<?> createCommentForPost(
            @Parameter(description = "ID do post")
            @PathVariable Long postId,
            @Valid @RequestBody Comment comment) {

        try {
            Comment createdComment = commentService.createForPost(postId, comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar comentário", description = "Atualiza todos os campos de um comentário")
    public ResponseEntity<?> updateComment(
            @Parameter(description = "ID do comentário")
            @PathVariable Long id,
            @Valid @RequestBody Comment commentDetails) {

        try {
            Comment updatedComment = commentService.update(id, commentDetails);
            return ResponseEntity.ok(updatedComment);
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
    @Operation(summary = "Atualizar comentário parcialmente", description = "Atualiza apenas os campos fornecidos")
    public ResponseEntity<?> partialUpdateComment(
            @Parameter(description = "ID do comentário")
            @PathVariable Long id,
            @RequestBody Comment commentDetails) {

        try {
            Comment updatedComment = commentService.partialUpdate(id, commentDetails);
            return ResponseEntity.ok(updatedComment);
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
    @Operation(summary = "Deletar comentário", description = "Remove um comentário do sistema")
    public ResponseEntity<?> deleteComment(
            @Parameter(description = "ID do comentário")
            @PathVariable Long id) {

        try {
            commentService.delete(id);
            return ResponseEntity.ok(new ApiResponse(true, "Comentário deletado com sucesso"));
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
    @Operation(summary = "Buscar comentários", description = "Busca comentários por email ou nome")
    public ResponseEntity<?> searchComments(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name) {

        try {
            if (email != null) {
                List<Comment> comments = commentService.findByEmail(email);
                return ResponseEntity.ok(comments);
            }

            if (name != null) {
                List<Comment> comments = commentService.findByName(name);
                return ResponseEntity.ok(comments);
            }

            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Pelo menos um parâmetro de busca é necessário"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar se comentário existe", description = "Verifica se um comentário existe pelo ID")
    public ResponseEntity<ApiResponse> commentExists(
            @Parameter(description = "ID do comentário")
            @PathVariable Long id) {

        try {
            boolean exists = commentService.existsById(id);
            return ResponseEntity.ok(new ApiResponse(exists,
                    exists ? "Comentário existe" : "Comentário não encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }
}