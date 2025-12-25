package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.dto.request.post.PostCreateRequest;
import vn.agriconnect.API.dto.request.post.PostFilterRequest;
import vn.agriconnect.API.dto.response.PostDetailResponse;
import vn.agriconnect.API.exception.ResourceNotFoundException;
import vn.agriconnect.API.mapper.PostMapper;
import vn.agriconnect.API.model.Post;
import vn.agriconnect.API.model.enums.PostStatus;
import vn.agriconnect.API.repository.PostRepository;
import vn.agriconnect.API.service.PostService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Override
    public PostDetailResponse create(String sellerId, PostCreateRequest request) {
        Post post = postMapper.toEntity(request);
        post.setSellerId(sellerId);
        post.setStatus(PostStatus.PENDING);
        post = postRepository.save(post);
        return postMapper.toResponse(post);
    }

    @Override
    public PostDetailResponse getById(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        return postMapper.toResponse(post);
    }

    @Override
    public PostDetailResponse update(String postId, PostCreateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setPrice(request.getPrice());
        post.setUnit(request.getUnit());
        post.setQuantity(request.getQuantity());
        post.setImages(request.getImages());
        post.setCategoryId(request.getCategoryId());
        post.setLocation(request.getLocation());
        
        post = postRepository.save(post);
        return postMapper.toResponse(post);
    }

    @Override
    public void delete(String postId) {
        postRepository.deleteById(postId);
    }

    @Override
    public List<PostDetailResponse> search(PostFilterRequest filter) {
        // TODO: Implement search with filters
        return postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.APPROVED)
                .stream()
                .map(postMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDetailResponse> getBySeller(String sellerId) {
        return postRepository.findBySellerId(sellerId)
                .stream()
                .map(postMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDetailResponse> getApproved() {
        return postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.APPROVED)
                .stream()
                .map(postMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void approve(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        post.setStatus(PostStatus.APPROVED);
        postRepository.save(post);
    }

    @Override
    public void reject(String postId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        post.setStatus(PostStatus.REJECTED);
        postRepository.save(post);
    }

    @Override
    public void incrementViewCount(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }
}
