package com.example.jsonplaceholderapi.repository;

import com.example.jsonplaceholderapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Métodos para autenticação
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Verificar se username/email já existem
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Buscar por username ou email (para login flexível)
    @Query("SELECT u FROM User u WHERE u.username = :credential OR u.email = :credential")
    Optional<User> findByUsernameOrEmail(@Param("credential") String credential);

    // Buscar usuários por cidade
    @Query("SELECT u FROM User u WHERE u.address.city = :city")
    Optional<User> findByCity(@Param("city") String city);

    // Buscar usuários por empresa
    @Query("SELECT u FROM User u WHERE u.company.name = :companyName")
    Optional<User> findByCompanyName(@Param("companyName") String companyName);
}