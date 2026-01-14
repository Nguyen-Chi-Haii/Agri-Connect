package vn.agriconnect.API.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import vn.agriconnect.API.model.Comment;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostId(String postId);
    Page<Comment> findByPostId(String postId, Pageable pageable);
    void deleteByPostId(String postId);
}
