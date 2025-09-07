package com.example.jsonplaceholderapi.repository;

import com.example.jsonplaceholderapi.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    // Buscar todos por usuário
    List<Todo> findByUserId(Long userId);
    Page<Todo> findByUserId(Long userId, Pageable pageable);

    // Buscar por status de conclusão
    List<Todo> findByUserIdAndCompleted(Long userId, Boolean completed);

    // Buscar por prioridade
    List<Todo> findByUserIdAndPriority(Long userId, Todo.Priority priority);

    // Buscar todos vencidos (que não foram completados e a data limite passou)
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND t.completed = false AND t.dueDate < :now")
    List<Todo> findOverdueTodos(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Buscar por título
    List<Todo> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);

    // Contar todos por usuário
    long countByUserId(Long userId);

    // Contar todos por status
    long countByUserIdAndCompleted(Long userId, Boolean completed);

    // Buscar todos com data limite próxima (próximos 7 dias)
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND t.completed = false AND t.dueDate BETWEEN :now AND :weekFromNow")
    List<Todo> findUpcomingTodos(@Param("userId") Long userId, @Param("now") LocalDateTime now, @Param("weekFromNow") LocalDateTime weekFromNow);
}