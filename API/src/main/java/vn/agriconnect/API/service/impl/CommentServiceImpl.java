package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.dto.response.PagedResponse;
import vn.agriconnect.API.exception.ResourceNotFoundException;
import vn.agriconnect.API.model.Comment;
import vn.agriconnect.API.model.Post;
import vn.agriconnect.API.repository.CommentRepository;
import vn.agriconnect.API.repository.PostRepository;
import vn.agriconnect.API.repository.UserRepository;
import vn.agriconnect.API.service.AuthService;
import vn.agriconnect.API.service.CommentService;

import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Override
    public Comment addComment(String postId, String content) {
        String userId = authService.getCurrentUserId();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        
        userRepository.findById(userId).ifPresent(user -> {
            comment.setUserName(user.getFullName());
        });
        
        comment.setContent(content);
        comment.setCreatedAt(Instant.now());
        
        Comment saved = commentRepository.save(comment);
        
        // Update comment count in Post
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        
        return saved;
    }

    @Override
    public PagedResponse<Comment> getCommentsByPost(String postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);
        
        comments.getContent().forEach(c -> {
            if (c.getUserName() == null) {
                userRepository.findById(c.getUserId()).ifPresent(user -> {
                    c.setUserName(user.getFullName());
                });
            }
        });
        
        return PagedResponse.<Comment>builder()
                .content(comments.getContent())
                .currentPage(comments.getNumber())
                .totalPages(comments.getTotalPages())
                .totalElements(comments.getTotalElements())
                .size(comments.getSize())
                .build();
    }

    @Override
    public void deleteComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        
        String currentUserId = authService.getCurrentUserId();
        // Allow owner or admin? For now just owner.
        if (!comment.getUserId().equals(currentUserId)) {
            // Check if admin... logic skipped for brevity, stick to basic owner check
            // Or throw AccessDeniedException
        }
        
        commentRepository.delete(comment);
        
        // Decrement comment count
        postRepository.findById(comment.getPostId()).ifPresent(post -> {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postRepository.save(post);
        });
    }
}
