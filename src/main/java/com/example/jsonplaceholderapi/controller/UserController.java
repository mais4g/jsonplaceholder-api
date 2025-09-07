package com.example.jsonplaceholderapi.controller;

import com.example.jsonplaceholderapi.dto.ApiResponse;
import com.example.jsonplaceholderapi.entity.User;
import com.example.jsonplaceholderapi.service.UserService;
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
@RequestMapping("/users")
@Tag(name = "Users", description = "Operações CRUD para usuários")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Listar todos os usuários", description = "Retorna lista paginada de usuários")
    public ResponseEntity<Page<User>> getAllUsers(
            @Parameter(description = "Número da página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<User> users = userService.findAll(pageable);

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos os usuários (sem paginação)", description = "Retorna lista completa de usuários")
    public ResponseEntity<List<User>> getAllUsersNoPagination() {
        try {
            List<User> users = userService.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna um usuário específico pelo ID")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID do usuário")
            @PathVariable Long id) {

        try {
            Optional<User> user = userService.findById(id);
            return user.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Criar novo usuário", description = "Cria um novo usuário no sistema")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.create(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza todos os campos de um usuário")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "ID do usuário")
            @PathVariable Long id,
            @Valid @RequestBody User userDetails) {

        try {
            User updatedUser = userService.update(id, userDetails);
            return ResponseEntity.ok(updatedUser);
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
    @Operation(summary = "Atualizar usuário parcialmente", description = "Atualiza apenas os campos fornecidos")
    public ResponseEntity<?> partialUpdateUser(
            @Parameter(description = "ID do usuário")
            @PathVariable Long id,
            @RequestBody User userDetails) {

        try {
            User updatedUser = userService.partialUpdate(id, userDetails);
            return ResponseEntity.ok(updatedUser);
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
    @Operation(summary = "Deletar usuário", description = "Remove um usuário do sistema")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID do usuário")
            @PathVariable Long id) {

        try {
            userService.delete(id);
            return ResponseEntity.ok(new ApiResponse(true, "Usuário deletado com sucesso"));
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
    @Operation(summary = "Buscar usuários", description = "Busca usuários por diferentes critérios")
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String company) {

        try {
            if (username != null) {
                Optional<User> user = userService.findByUsername(username);
                return user.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
            }

            if (email != null) {
                Optional<User> user = userService.findByEmail(email);
                return user.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
            }

            if (city != null) {
                Optional<User> user = userService.findByCity(city);
                return user.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
            }

            if (company != null) {
                Optional<User> user = userService.findByCompanyName(company);
                return user.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
            }

            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Pelo menos um parâmetro de busca é necessário"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar se usuário existe", description = "Verifica se um usuário existe pelo ID")
    public ResponseEntity<ApiResponse> userExists(
            @Parameter(description = "ID do usuário")
            @PathVariable Long id) {

        try {
            boolean exists = userService.existsById(id);
            return ResponseEntity.ok(new ApiResponse(exists,
                    exists ? "Usuário existe" : "Usuário não encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }
}