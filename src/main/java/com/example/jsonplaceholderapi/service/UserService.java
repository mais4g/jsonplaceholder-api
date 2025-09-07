package com.example.jsonplaceholderapi.service;

import com.example.jsonplaceholderapi.entity.User;
import com.example.jsonplaceholderapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Buscar todos os usuários
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // Buscar todos com paginação
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // Buscar por ID
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Buscar por username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Buscar por email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Buscar por username ou email
    public Optional<User> findByUsernameOrEmail(String credential) {
        return userRepository.findByUsernameOrEmail(credential);
    }

    // Criar usuário
    public User create(User user) {
        // Validar se username já existe
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username já está em uso: " + user.getUsername());
        }

        // Validar se email já existe
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email já está em uso: " + user.getEmail());
        }

        // Criptografar senha se não estiver criptografada
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    // Atualizar usuário
    public User update(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        // Verificar se novo username já existe (se diferente do atual)
        if (!user.getUsername().equals(userDetails.getUsername()) &&
                userRepository.existsByUsername(userDetails.getUsername())) {
            throw new RuntimeException("Username já está em uso: " + userDetails.getUsername());
        }

        // Verificar se novo email já existe (se diferente do atual)
        if (!user.getEmail().equals(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email já está em uso: " + userDetails.getEmail());
        }

        // Atualizar campos
        user.setName(userDetails.getName());
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setWebsite(userDetails.getWebsite());
        user.setAddress(userDetails.getAddress());
        user.setCompany(userDetails.getCompany());

        // Atualizar senha apenas se fornecida
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    // Atualizar parcialmente
    public User partialUpdate(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        // Atualizar apenas campos não nulos
        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getUsername() != null && !user.getUsername().equals(userDetails.getUsername())) {
            if (userRepository.existsByUsername(userDetails.getUsername())) {
                throw new RuntimeException("Username já está em uso: " + userDetails.getUsername());
            }
            user.setUsername(userDetails.getUsername());
        }
        if (userDetails.getEmail() != null && !user.getEmail().equals(userDetails.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("Email já está em uso: " + userDetails.getEmail());
            }
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }
        if (userDetails.getWebsite() != null) {
            user.setWebsite(userDetails.getWebsite());
        }
        if (userDetails.getAddress() != null) {
            user.setAddress(userDetails.getAddress());
        }
        if (userDetails.getCompany() != null) {
            user.setCompany(userDetails.getCompany());
        }
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    // Deletar usuário
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        userRepository.delete(user);
    }

    // Verificar se usuário existe
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    // Verificar se username existe
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // Verificar se email existe
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Buscar por cidade
    public Optional<User> findByCity(String city) {
        return userRepository.findByCity(city);
    }

    // Buscar por empresa
    public Optional<User> findByCompanyName(String companyName) {
        return userRepository.findByCompanyName(companyName);
    }
}