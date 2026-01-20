package vn.agriconnect.API.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.agriconnect.API.dto.request.post.PostCreateRequest;
import vn.agriconnect.API.dto.response.PostDetailResponse;
import vn.agriconnect.API.model.Post;
import vn.agriconnect.API.repository.CategoryRepository;
import vn.agriconnect.API.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public Post toEntity(PostCreateRequest request) {
        if (request == null) return null;
        
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setPrice(request.getPrice());
        post.setUnit(request.getUnit());
        post.setQuantity(request.getQuantity());
        post.setImages(request.getImages());
        post.setCategoryId(request.getCategoryId());
        post.setLocation(request.getLocation());
        return post;
    }

    public PostDetailResponse toResponse(Post post) {
        if (post == null) return null;
        
        PostDetailResponse.PostDetailResponseBuilder builder = PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .price(post.getPrice())
                .unit(post.getUnit())
                .quantity(post.getQuantity())
                .images(post.getImages())
                .categoryId(post.getCategoryId())
                .sellerId(post.getSellerId())
                .location(post.getLocation())
                .status(post.getStatus())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt());

        // Fill seller info
        if (post.getSellerId() != null) {
            userRepository.findById(post.getSellerId()).ifPresent(user -> {
                builder.sellerName(user.getFullName());
                builder.sellerPhone(user.getPhone());
                builder.sellerAvatar(user.getAvatar());
            });
        }

        // Fill category info
        if (post.getCategoryId() != null) {
            categoryRepository.findById(post.getCategoryId()).ifPresent(category -> {
                builder.categoryName(category.getName());
            });
        }

        return builder.build();
    }
}
