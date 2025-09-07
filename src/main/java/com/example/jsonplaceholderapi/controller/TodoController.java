package com.example.jsonplaceholderapi.controller;

import com.example.jsonplaceholderapi.dto.ApiResponse;
import com.example.jsonplaceholderapi.entity.Todo;
import com.example.jsonplaceholderapi.service.TodoService;
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
@RequestMapping("/todos")
@Tag(name = "Todos", description = "Operações CRUD para tarefas")
@SecurityRequirement(name = "Bearer Authentication")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @GetMapping
    @Operation(summary = "Listar todas as tarefas", description = "Retorna lista paginada de tarefas")
    public ResponseEntity<Page<Todo>> getAllTodos(
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
            Page<Todo> todos = todoService.findAll(pageable);

            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas as tarefas (sem paginação)", description = "Retorna lista completa de tarefas")
    public ResponseEntity<List<Todo>> getAllTodosNoPagination() {
        try {
            List<Todo> todos = todoService.findAll();
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tarefa por ID", description = "Retorna uma tarefa específica pelo ID")
    public ResponseEntity<Todo> getTodoById(
            @Parameter(description = "ID da tarefa")
            @PathVariable Long id) {

        try {
            Optional<Todo> todo = todoService.findById(id);
            return todo.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Buscar tarefas por usuário", description = "Retorna tarefas de um usuário específico")
    public ResponseEntity<Page<Todo>> getTodosByUser(
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
            Page<Todo> todos = todoService.findByUserId(userId, pageable);

            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/completed")
    @Operation(summary = "Buscar tarefas por status", description = "Retorna tarefas completas ou pendentes de um usuário")
    public ResponseEntity<List<Todo>> getTodosByStatus(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId,
            @Parameter(description = "Status de conclusão")
            @RequestParam Boolean completed) {

        try {
            List<Todo> todos = todoService.findByUserIdAndCompleted(userId, completed);
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/priority/{priority}")
    @Operation(summary = "Buscar tarefas por prioridade", description = "Retorna tarefas de uma prioridade específica")
    public ResponseEntity<List<Todo>> getTodosByPriority(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId,
            @Parameter(description = "Prioridade da tarefa")
            @PathVariable Todo.Priority priority) {

        try {
            List<Todo> todos = todoService.findByUserIdAndPriority(userId, priority);
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/overdue")
    @Operation(summary = "Buscar tarefas vencidas", description = "Retorna tarefas vencidas de um usuário")
    public ResponseEntity<List<Todo>> getOverdueTodos(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId) {

        try {
            List<Todo> todos = todoService.findOverdueTodos(userId);
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/upcoming")
    @Operation(summary = "Buscar tarefas próximas", description = "Retorna tarefas com vencimento nos próximos 7 dias")
    public ResponseEntity<List<Todo>> getUpcomingTodos(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId) {

        try {
            List<Todo> todos = todoService.findUpcomingTodos(userId);
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}/stats")
    @Operation(summary = "Estatísticas das tarefas", description = "Retorna estatísticas das tarefas do usuário")
    public ResponseEntity<TodoService.TodoStats> getTodoStats(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId) {

        try {
            TodoService.TodoStats stats = todoService.getUserTodoStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Criar nova tarefa", description = "Cria uma nova tarefa no sistema")
    public ResponseEntity<?> createTodo(@Valid @RequestBody Todo todo) {
        try {
            Todo createdTodo = todoService.create(todo);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PostMapping("/user/{userId}")
    @Operation(summary = "Criar tarefa para usuário", description = "Cria uma nova tarefa para um usuário específico")
    public ResponseEntity<?> createTodoForUser(
            @Parameter(description = "ID do usuário")
            @PathVariable Long userId,
            @Valid @RequestBody Todo todo) {

        try {
            Todo createdTodo = todoService.createForUser(userId, todo);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tarefa", description = "Atualiza todos os campos de uma tarefa")
    public ResponseEntity<?> updateTodo(
            @Parameter(description = "ID da tarefa")
            @PathVariable Long id,
            @Valid @RequestBody Todo todoDetails) {

        try {
            Todo updatedTodo = todoService.update(id, todoDetails);
            return ResponseEntity.ok(updatedTodo);
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
    @Operation(summary = "Atualizar tarefa parcialmente", description = "Atualiza apenas os campos fornecidos")
    public ResponseEntity<?> partialUpdateTodo(
            @Parameter(description = "ID da tarefa")
            @PathVariable Long id,
            @RequestBody Todo todoDetails) {

        try {
            Todo updatedTodo = todoService.partialUpdate(id, todoDetails);
            return ResponseEntity.ok(updatedTodo);
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

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Marcar tarefa como completa", description = "Marca uma tarefa como completa")
    public ResponseEntity<?> completeTodo(
            @Parameter(description = "ID da tarefa")
            @PathVariable Long id) {

        try {
            Todo completedTodo = todoService.markAsCompleted(id);
            return ResponseEntity.ok(completedTodo);
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

    @PatchMapping("/{id}/incomplete")
    @Operation(summary = "Marcar tarefa como incompleta", description = "Marca uma tarefa como incompleta")
    public ResponseEntity<?> incompleteTodo(
            @Parameter(description = "ID da tarefa")
            @PathVariable Long id) {

        try {
            Todo incompleteTodo = todoService.markAsIncomplete(id);
            return ResponseEntity.ok(incompleteTodo);
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
    @Operation(summary = "Deletar tarefa", description = "Remove uma tarefa do sistema")
    public ResponseEntity<?> deleteTodo(
            @Parameter(description = "ID da tarefa")
            @PathVariable Long id) {

        try {
            todoService.delete(id);
            return ResponseEntity.ok(new ApiResponse(true, "Tarefa deletada com sucesso"));
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
    @Operation(summary = "Buscar tarefas", description = "Busca tarefas por título")
    public ResponseEntity<?> searchTodos(
            @RequestParam Long userId,
            @RequestParam(required = false) String title) {

        try {
            if (title != null) {
                List<Todo> todos = todoService.findByTitle(userId, title);
                return ResponseEntity.ok(todos);
            }

            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Parâmetro 'title' é necessário"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar se tarefa existe", description = "Verifica se uma tarefa existe pelo ID")
    public ResponseEntity<ApiResponse> todoExists(
            @Parameter(description = "ID da tarefa")
            @PathVariable Long id) {

        try {
            boolean exists = todoService.existsById(id);
            return ResponseEntity.ok(new ApiResponse(exists,
                    exists ? "Tarefa existe" : "Tarefa não encontrada"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }
}