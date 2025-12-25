package vn.agriconnect.API.mapper;

import org.springframework.stereotype.Component;
import vn.agriconnect.API.dto.request.post.PostCreateRequest;
import vn.agriconnect.API.dto.response.PostDetailResponse;
import vn.agriconnect.API.model.Post;

@Component
public class PostMapper {

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
        
        return PostDetailResponse.builder()
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
                .createdAt(post.getCreatedAt())
                .build();
    }
}
