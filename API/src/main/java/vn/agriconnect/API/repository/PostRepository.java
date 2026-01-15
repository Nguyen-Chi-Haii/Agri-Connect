package vn.agriconnect.API.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.agriconnect.API.model.Post;
import vn.agriconnect.API.model.enums.PostStatus;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findBySellerId(String sellerId);

    List<Post> findByCategoryId(String categoryId);

    List<Post> findByStatus(PostStatus status);

    List<Post> findByStatusOrderByCreatedAtDesc(PostStatus status);

    List<Post> findBySellerIdAndStatus(String sellerId, PostStatus status);

    long countBySellerId(String sellerId);

    long countBySellerIdAndStatus(String sellerId, PostStatus status);

    long countByCategoryId(String categoryId);
}
