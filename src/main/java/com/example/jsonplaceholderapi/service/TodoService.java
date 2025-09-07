package com.example.jsonplaceholderapi.service;

import com.example.jsonplaceholderapi.entity.Todo;
import com.example.jsonplaceholderapi.entity.User;
import com.example.jsonplaceholderapi.repository.TodoRepository;
import com.example.jsonplaceholderapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    // Buscar todas as tarefas
    public List<Todo> findAll() {
        return todoRepository.findAll();
    }

    // Buscar todas com paginação
    public Page<Todo> findAll(Pageable pageable) {
        return todoRepository.findAll(pageable);
    }

    // Buscar por ID
    public Optional<Todo> findById(Long id) {
        return todoRepository.findById(id);
    }

    // Buscar tarefas por usuário
    public List<Todo> findByUserId(Long userId) {
        return todoRepository.findByUserId(userId);
    }

    // Buscar tarefas por usuário com paginação
    public Page<Todo> findByUserId(Long userId, Pageable pageable) {
        return todoRepository.findByUserId(userId, pageable);
    }

    // Buscar por status de conclusão
    public List<Todo> findByUserIdAndCompleted(Long userId, Boolean completed) {
        return todoRepository.findByUserIdAndCompleted(userId, completed);
    }

    // Buscar por prioridade
    public List<Todo> findByUserIdAndPriority(Long userId, Todo.Priority priority) {
        return todoRepository.findByUserIdAndPriority(userId, priority);
    }

    // Buscar tarefas vencidas
    public List<Todo> findOverdueTodos(Long userId) {
        return todoRepository.findOverdueTodos(userId, LocalDateTime.now());
    }

    // Buscar tarefas próximas (próximos 7 dias)
    public List<Todo> findUpcomingTodos(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekFromNow = now.plusDays(7);
        return todoRepository.findUpcomingTodos(userId, now, weekFromNow);
    }

    // Buscar por título
    public List<Todo> findByTitle(Long userId, String title) {
        return todoRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title);
    }

    // Contar tarefas por status
    public long countByUserIdAndCompleted(Long userId, Boolean completed) {
        return todoRepository.countByUserIdAndCompleted(userId, completed);
    }

    // Criar tarefa
    public Todo create(Todo todo) {
        // Validar se usuário existe
        if (todo.getUser() != null && todo.getUser().getId() != null) {
            User user = userRepository.findById(todo.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + todo.getUser().getId()));
            todo.setUser(user);
        } else {
            throw new RuntimeException("ID do usuário é obrigatório");
        }

        return todoRepository.save(todo);
    }

    // Criar tarefa para usuário específico
    public Todo createForUser(Long userId, Todo todo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));

        todo.setUser(user);
        return todoRepository.save(todo);
    }

    // Atualizar tarefa
    public Todo update(Long id, Todo todoDetails) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada com ID: " + id));

        // Atualizar campos
        todo.setTitle(todoDetails.getTitle());
        todo.setDescription(todoDetails.getDescription());
        todo.setCompleted(todoDetails.getCompleted());
        todo.setPriority(todoDetails.getPriority());
        todo.setDueDate(todoDetails.getDueDate());

        // Atualizar usuário se fornecido
        if (todoDetails.getUser() != null && todoDetails.getUser().getId() != null) {
            User user = userRepository.findById(todoDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + todoDetails.getUser().getId()));
            todo.setUser(user);
        }

        return todoRepository.save(todo);
    }

    // Atualizar parcialmente
    public Todo partialUpdate(Long id, Todo todoDetails) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada com ID: " + id));

        // Atualizar apenas campos não nulos
        if (todoDetails.getTitle() != null) {
            todo.setTitle(todoDetails.getTitle());
        }
        if (todoDetails.getDescription() != null) {
            todo.setDescription(todoDetails.getDescription());
        }
        if (todoDetails.getCompleted() != null) {
            todo.setCompleted(todoDetails.getCompleted());
        }
        if (todoDetails.getPriority() != null) {
            todo.setPriority(todoDetails.getPriority());
        }
        if (todoDetails.getDueDate() != null) {
            todo.setDueDate(todoDetails.getDueDate());
        }
        if (todoDetails.getUser() != null && todoDetails.getUser().getId() != null) {
            User user = userRepository.findById(todoDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + todoDetails.getUser().getId()));
            todo.setUser(user);
        }

        return todoRepository.save(todo);
    }

    // Marcar como completa
    public Todo markAsCompleted(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada com ID: " + id));

        todo.markAsCompleted();
        return todoRepository.save(todo);
    }

    // Marcar como incompleta
    public Todo markAsIncomplete(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada com ID: " + id));

        todo.markAsIncomplete();
        return todoRepository.save(todo);
    }

    // Deletar tarefa
    public void delete(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada com ID: " + id));

        todoRepository.delete(todo);
    }

    // Verificar se tarefa existe
    public boolean existsById(Long id) {
        return todoRepository.existsById(id);
    }

    // Verificar se usuário pode editar tarefa
    public boolean canUserEditTodo(Long todoId, Long userId) {
        Optional<Todo> todo = todoRepository.findById(todoId);
        return todo.isPresent() && todo.get().getUser().getId().equals(userId);
    }

    // Estatísticas do usuário
    public TodoStats getUserTodoStats(Long userId) {
        long total = todoRepository.countByUserId(userId);
        long completed = todoRepository.countByUserIdAndCompleted(userId, true);
        long pending = todoRepository.countByUserIdAndCompleted(userId, false);
        long overdue = todoRepository.findOverdueTodos(userId, LocalDateTime.now()).size();

        return new TodoStats(total, completed, pending, overdue);
    }

    // Classe interna para estatísticas
    public static class TodoStats {
        private long total;
        private long completed;
        private long pending;
        private long overdue;

        public TodoStats(long total, long completed, long pending, long overdue) {
            this.total = total;
            this.completed = completed;
            this.pending = pending;
            this.overdue = overdue;
        }

        // Getters
        public long getTotal() { return total; }
        public long getCompleted() { return completed; }
        public long getPending() { return pending; }
        public long getOverdue() { return overdue; }
    }
}