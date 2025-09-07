package com.example.jsonplaceholderapi.service;

import com.example.jsonplaceholderapi.entity.Comment;
import com.example.jsonplaceholderapi.entity.Post;
import com.example.jsonplaceholderapi.entity.User;
import com.example.jsonplaceholderapi.repository.CommentRepository;
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
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // Buscar todos os comentários
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    // Buscar todos com paginação
    public Page<Comment> findAll(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }

    // Buscar por ID
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    // Buscar comentários por post
    public List<Comment> findByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    // Buscar comentários por post com paginação
    public Page<Comment> findByPostId(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable);
    }

    // Buscar por email
    public List<Comment> findByEmail(String email) {
        return commentRepository.findByEmail(email);
    }

    // Buscar por nome
    public List<Comment> findByName(String name) {
        return commentRepository.findByNameContainingIgnoreCase(name);
    }

    // Contar comentários por post
    public long countByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    // Criar comentário
    public Comment create(Comment comment) {
        // Validar se post existe
        if (comment.getPost() != null && comment.getPost().getId() != null) {
            Post post = postRepository.findById(comment.getPost().getId())
                    .orElseThrow(() -> new RuntimeException("Post não encontrado com ID: " + comment.getPost().getId()));
            comment.setPost(post);
        } else {
            throw new RuntimeException("ID do post é obrigatório");
        }

        // Validar usuário se fornecido (comentário pode ser anônimo)
        if (comment.getUser() != null && comment.getUser().getId() != null) {
            User user = userRepository.findById(comment.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + comment.getUser().getId()));
            comment.setUser(user);
        }

        return commentRepository.save(comment);
    }

    // Criar comentário para post específico
    public Comment createForPost(Long postId, Comment comment) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado com ID: " + postId));

        comment.setPost(post);

        // Validar usuário se fornecido
        if (comment.getUser() != null && comment.getUser().getId() != null) {
            User user = userRepository.findById(comment.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + comment.getUser().getId()));
            comment.setUser(user);
        }

        return commentRepository.save(comment);
    }

    // Atualizar comentário
    public Comment update(Long id, Comment commentDetails) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado com ID: " + id));

        // Atualizar campos
        comment.setName(commentDetails.getName());
        comment.setEmail(commentDetails.getEmail());
        comment.setBody(commentDetails.getBody());

        // Atualizar post se fornecido
        if (commentDetails.getPost() != null && commentDetails.getPost().getId() != null) {
            Post post = postRepository.findById(commentDetails.getPost().getId())
                    .orElseThrow(() -> new RuntimeException("Post não encontrado com ID: " + commentDetails.getPost().getId()));
            comment.setPost(post);
        }

        // Atualizar usuário se fornecido
        if (commentDetails.getUser() != null && commentDetails.getUser().getId() != null) {
            User user = userRepository.findById(commentDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + commentDetails.getUser().getId()));
            comment.setUser(user);
        }

        return commentRepository.save(comment);
    }

    // Atualizar parcialmente
    public Comment partialUpdate(Long id, Comment commentDetails) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado com ID: " + id));

        // Atualizar apenas campos não nulos
        if (commentDetails.getName() != null) {
            comment.setName(commentDetails.getName());
        }
        if (commentDetails.getEmail() != null) {
            comment.setEmail(commentDetails.getEmail());
        }
        if (commentDetails.getBody() != null) {
            comment.setBody(commentDetails.getBody());
        }
        if (commentDetails.getPost() != null && commentDetails.getPost().getId() != null) {
            Post post = postRepository.findById(commentDetails.getPost().getId())
                    .orElseThrow(() -> new RuntimeException("Post não encontrado com ID: " + commentDetails.getPost().getId()));
            comment.setPost(post);
        }
        if (commentDetails.getUser() != null && commentDetails.getUser().getId() != null) {
            User user = userRepository.findById(commentDetails.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + commentDetails.getUser().getId()));
            comment.setUser(user);
        }

        return commentRepository.save(comment);
    }

    // Deletar comentário
    public void delete(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado com ID: " + id));

        commentRepository.delete(comment);
    }

    // Verificar se comentário existe
    public boolean existsById(Long id) {
        return commentRepository.existsById(id);
    }

    // Verificar se usuário pode editar comentário
    public boolean canUserEditComment(Long commentId, Long userId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        return comment.isPresent() &&
                comment.get().getUser() != null &&
                comment.get().getUser().getId().equals(userId);
    }
}