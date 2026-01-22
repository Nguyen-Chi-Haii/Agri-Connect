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
    private final vn.agriconnect.API.service.AuthService authService;
    private final vn.agriconnect.API.repository.UserRepository userRepository;
    private final vn.agriconnect.API.service.NotificationService notificationService;

    @Override
    public PostDetailResponse create(String sellerId, PostCreateRequest request) {
        vn.agriconnect.API.model.User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", sellerId));

        if (seller.getKyc() == null || !"VERIFIED".equals(seller.getKyc().getStatus())) {
            throw new vn.agriconnect.API.exception.BadRequestException("Bạn cần xác minh danh tính thành công để đăng bài");
        }

        Post post = postMapper.toEntity(request);
        post.setSellerId(sellerId);
        post.setStatus(PostStatus.PENDING); // Still pending approval for content
        post = postRepository.save(post);
        
        // --- Notification Trigger: Notify All Admins ---
        java.util.List<vn.agriconnect.API.model.User> admins = userRepository.findByRole(vn.agriconnect.API.model.enums.Role.ADMIN);
        for (vn.agriconnect.API.model.User admin : admins) {
            notificationService.create(
                admin.getId(),
                "Bài đăng mới cần duyệt",
                "Người dùng " + seller.getFullName() + " vừa đăng bài viết mới: " + post.getTitle()
            );
        }
        
        return toDetailResponse(post);
    }

    @Override
    public PostDetailResponse getById(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        return toDetailResponse(post);
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
        return toDetailResponse(post);
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
        
        // Filter by status
        if (filter.getStatus() != null) {
            query.addCriteria(Criteria.where("status").is(filter.getStatus()));
        } else {
            // Logic: If status is null (viewing "All")
            // - If ADMIN: show everything (don't filter status)
            // - If User/Guest: show only APPROVED
            
            boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                query.addCriteria(Criteria.where("status").is(PostStatus.APPROVED));
            }
            // If Admin, do nothing -> mean return all status
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
                .map(this::toDetailResponse)
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
    public List<PostDetailResponse> getBySeller(String sellerId, PostStatus status) {
        List<Post> posts;
        if (status != null) {
            posts = postRepository.findBySellerIdAndStatus(sellerId, status);
        } else {
            posts = postRepository.findBySellerId(sellerId);
        }
        return posts.stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDetailResponse> getApproved() {
        return postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.APPROVED)
                .stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void approve(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        post.setStatus(PostStatus.APPROVED);
        postRepository.save(post);
        
        // --- Notification Trigger ---
        notificationService.create(
            post.getSellerId(),
            "Bài đăng đã được duyệt",
            "Bài đăng '" + post.getTitle() + "' của bạn đã được duyệt và hiển thị trên chợ."
        );
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void reject(String postId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        post.setStatus(PostStatus.REJECTED);
        postRepository.save(post);
        
        // --- Notification Trigger ---
        notificationService.create(
            post.getSellerId(),
            "Bài đăng bị từ chối",
            "Bài đăng '" + post.getTitle() + "' đã bị từ chối. Lý do: " + reason
        );
    }

    @Override
    public void incrementViewCount(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    @Override
    public vn.agriconnect.API.dto.response.PostInteractionResponse toggleLike(String postId) {
        String currentUserId = authService.getCurrentUserId();
        if (currentUserId == null) {
            throw new vn.agriconnect.API.exception.BadRequestException("Authentication required to like post");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        Query query = new Query(Criteria.where("id").is(postId));
        boolean alreadyLiked = post.getLikedUserIds().contains(currentUserId);
        
        org.springframework.data.mongodb.core.query.Update update = new org.springframework.data.mongodb.core.query.Update();
        if (alreadyLiked) {
            update.pull("likedUserIds", currentUserId);
        } else {
            update.addToSet("likedUserIds", currentUserId);
        }
        
        mongoTemplate.updateFirst(query, update, Post.class);
        
        // Fetch updated state for response
        Post updatedPost = postRepository.findById(postId).get();
        return vn.agriconnect.API.dto.response.PostInteractionResponse.builder()
                .likeCount(updatedPost.getLikedUserIds().size())
                .commentCount(updatedPost.getCommentCount())
                .isLiked(updatedPost.getLikedUserIds().contains(currentUserId))
                .build();
    }

    @Override
    public void close(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        String currentUserId = authService.getCurrentUserId();
        if (currentUserId == null) {
            throw new vn.agriconnect.API.exception.BadRequestException("Authentication required");
        }
        
        // Check if admin
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // Check if owner
        if (!isAdmin && !post.getSellerId().equals(currentUserId)) {
             throw new vn.agriconnect.API.exception.BadRequestException("You do not have permission to close this post");
        }
        
        post.setStatus(PostStatus.CLOSED);
        postRepository.save(post);
    }

    private PostDetailResponse toDetailResponse(Post post) {
        PostDetailResponse response = postMapper.toResponse(post);
        
        // Enrich interaction data
        response.setLikeCount(post.getLikedUserIds().size());
        response.setCommentCount(post.getCommentCount());
        
        String currentUserId = authService.getCurrentUserId();
        if (currentUserId != null) {
            response.setLiked(post.getLikedUserIds().contains(currentUserId));
        } else {
            response.setLiked(false);
        }
        
        return response;
    }
}

