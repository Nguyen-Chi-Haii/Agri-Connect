package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.agriconnect.API.dto.request.post.PostCreateRequest;
import vn.agriconnect.API.dto.request.post.PostFilterRequest;
import vn.agriconnect.API.dto.response.PagedResponse;
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
@SuppressWarnings("null")
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final MongoTemplate mongoTemplate;

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
    public PagedResponse<PostDetailResponse> search(PostFilterRequest filter) {
        Query query = new Query();
        
        // Filter by keyword (search in title and description)
        if (StringUtils.hasText(filter.getKeyword())) {
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("title").regex(filter.getKeyword(), "i"),
                    Criteria.where("description").regex(filter.getKeyword(), "i")
            );
            query.addCriteria(keywordCriteria);
        }
        
        // Filter by category
        if (StringUtils.hasText(filter.getCategoryId())) {
            query.addCriteria(Criteria.where("categoryId").is(filter.getCategoryId()));
        }
        
        // Filter by location (province)
        if (StringUtils.hasText(filter.getProvince())) {
            query.addCriteria(Criteria.where("location.province").is(filter.getProvince()));
        }
        
        // Filter by location (district)
        if (StringUtils.hasText(filter.getDistrict())) {
            query.addCriteria(Criteria.where("location.district").is(filter.getDistrict()));
        }
        
        // Filter by price range
        if (filter.getMinPrice() != null) {
            query.addCriteria(Criteria.where("price").gte(filter.getMinPrice()));
        }
        if (filter.getMaxPrice() != null) {
            query.addCriteria(Criteria.where("price").lte(filter.getMaxPrice()));
        }
        
        // Filter by status (default to APPROVED for public search)
        if (filter.getStatus() != null) {
            query.addCriteria(Criteria.where("status").is(filter.getStatus()));
        } else {
            query.addCriteria(Criteria.where("status").is(PostStatus.APPROVED));
        }
        
        // Sorting
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (StringUtils.hasText(filter.getSortBy())) {
            Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortOrder()) 
                    ? Sort.Direction.ASC 
                    : Sort.Direction.DESC;
            sort = Sort.by(direction, filter.getSortBy());
        }
        
        // Pagination
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, sort);
        
        query.with(pageable);
        
        // Execute query
        List<Post> posts = mongoTemplate.find(query, Post.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Post.class);
        
        List<PostDetailResponse> content = posts.stream()
                .map(postMapper::toResponse)
                .collect(Collectors.toList());
        
        int totalPages = (int) Math.ceil((double) total / size);
        
        return PagedResponse.<PostDetailResponse>builder()
                .content(content)
                .currentPage(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
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
    @PreAuthorize("hasRole('ADMIN')")
    public void approve(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        post.setStatus(PostStatus.APPROVED);
        postRepository.save(post);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
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

