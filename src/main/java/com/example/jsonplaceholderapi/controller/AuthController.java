package com.example.jsonplaceholderapi.controller;

import com.example.jsonplaceholderapi.dto.ApiResponse;
import com.example.jsonplaceholderapi.dto.AuthResponse;
import com.example.jsonplaceholderapi.dto.LoginRequest;
import com.example.jsonplaceholderapi.dto.SignupRequest;
import com.example.jsonplaceholderapi.entity.User;
import com.example.jsonplaceholderapi.repository.UserRepository;
import com.example.jsonplaceholderapi.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints para autenticação de usuários")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Autentica usuário e retorna token JWT")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar usuário
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Obter detalhes do usuário autenticado
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Gerar token JWT
            String jwt = jwtUtil.generateTokenWithUserId(userDetails, user.getId());

            // Retornar resposta com token
            AuthResponse authResponse = new AuthResponse(
                    jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getName()
            );

            return ResponseEntity.ok(authResponse);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Credenciais inválidas"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro interno do servidor"));
        }
    }

    @PostMapping("/signup")
    @Operation(summary = "Registro de usuário", description = "Registra novo usuário no sistema")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            // Verificar se username já existe
            if (userRepository.existsByUsername(signupRequest.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Username já está em uso"));
            }

            // Verificar se email já existe
            if (userRepository.existsByEmail(signupRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email já está em uso"));
            }

            // Criar novo usuário
            User user = new User();
            user.setName(signupRequest.getName());
            user.setUsername(signupRequest.getUsername());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            user.setPhone(signupRequest.getPhone());
            user.setWebsite(signupRequest.getWebsite());

            // Salvar usuário
            User savedUser = userRepository.save(user);

            // Gerar token JWT para login automático
            String jwt = jwtUtil.generateTokenWithUserId(savedUser, savedUser.getId());

            // Retornar resposta com token
            AuthResponse authResponse = new AuthResponse(
                    jwt,
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getName()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erro ao criar usuário: " + e.getMessage()));
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validar token", description = "Valida se o token JWT é válido")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Token não fornecido ou formato inválido"));
            }

            String token = authHeader.substring(7);
            boolean isValid = jwtUtil.isTokenValid(token);

            if (isValid) {
                String username = jwtUtil.extractUsername(token);
                User user = userRepository.findByUsernameOrEmail(username)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                AuthResponse authResponse = new AuthResponse(
                        token,
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getName()
                );

                return ResponseEntity.ok(authResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Token inválido ou expirado"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Erro ao validar token"));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Obter usuário atual", description = "Retorna dados do usuário autenticado")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Token não fornecido"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            User user = userRepository.findByUsernameOrEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Retornar dados do usuário (sem senha)
            AuthResponse userInfo = new AuthResponse(
                    null, // Não retornar token
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getName()
            );

            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Erro ao obter dados do usuário"));
        }
    }
}