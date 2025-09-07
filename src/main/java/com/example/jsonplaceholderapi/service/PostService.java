package com.example.jsonplaceholderapi.service;

import com.example.jsonplaceholderapi.entity.Post;
import com.example.jsonplaceholderapi.entity.User;
import com.example.jsonplaceholderapi.repository.PostRepository;
import com.example.jsonplaceholderapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // Buscar todos os posts
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    // Buscar todos com paginação
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    // Buscar por ID
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    // Buscar posts por usuário
    public List<Post> findByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }

    // Buscar posts por usuário com paginação
    public Page<Post> findByUserId(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable);
    }

    // Buscar por título
    public List<Post> findByTitle(String title) {
        return postRepository.findByTitleContainingIgnoreCase(title);
    }

    // Buscar por conteúdo
    public List<Post> findByContent(String content) {
        return postRepository.findByContentContaining(content);
    }

    // Buscar posts mais recentes
    public Page<Post> findLatestPosts(Pageable pageable) {
        return postRepository.findLatestPosts(pageable);
    }

    // Criar post
    public Post create(Post post) {
        // Validar se usuário existe
        if (post.getUser() != null && post.getUser().getId() != null) {
            User user = userRepository.findById(post.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + post.getUser().getId()));
            post.setUser(user);
        } else {
            throw new RuntimeException("ID do usuário é obrigatório");
        }

        return postRepository.save(post);
    }

    // Criar post com userId
    public Post create(Post post, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + userId));

        post.setUser(user);
        return postRepository.save(post);
    }

    // Atualizar post
    public Post update(Long id, Post postDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado com ID: " + id));

        // Atualizar campos
        post.setTitle(postDetails.getTitle());
        post.setBody(postDetails.getBody());

        // Atualizar usuário se fornecido
        if (postDetails.getUser() != null && postDetails.getUser().getId() != null) {
            User user = userRepository.findById(postDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + postDetails.getUser().getId()));
            post.setUser(user);
        }

        return postRepository.save(post);
    }

    // Atualizar parcialmente
    public Post partialUpdate(Long id, Post postDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado com ID: " + id));

        // Atualizar apenas campos não nulos
        if (postDetails.getTitle() != null) {
            post.setTitle(postDetails.getTitle());
        }
        if (postDetails.getBody() != null) {
            post.setBody(postDetails.getBody());
        }
        if (postDetails.getUser() != null && postDetails.getUser().getId() != null) {
            User user = userRepository.findById(postDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + postDetails.getUser().getId()));
            post.setUser(user);
        }

        return postRepository.save(post);
    }

    // Deletar post
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post não encontrado com ID: " + id));

        postRepository.delete(post);
    }

    // Verificar se post existe
    public boolean existsById(Long id) {
        return postRepository.existsById(id);
    }

    // Verificar se usuário pode editar post
    public boolean canUserEditPost(Long postId, Long userId) {
        Optional<Post> post = postRepository.findById(postId);
        return post.isPresent() && post.get().getUser().getId().equals(userId);
    }
}