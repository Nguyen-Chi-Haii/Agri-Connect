package vn.agriconnect.API.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.request.post.PostCreateRequest;
import vn.agriconnect.API.dto.request.post.PostFilterRequest;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.dto.response.PagedResponse;
import vn.agriconnect.API.dto.response.PostDetailResponse;
import vn.agriconnect.API.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> create(
            @Valid @RequestBody PostCreateRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        // Debug log for images
        log.info("Creating post - Title: {}, Images count: {}",
                request.getTitle(),
                request.getImages() != null ? request.getImages().size() : 0);

        PostDetailResponse post = postService.create(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Bài đăng đã được tạo", post));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getById(@PathVariable String postId) {
        postService.incrementViewCount(postId);
        PostDetailResponse post = postService.getById(postId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> update(
            @PathVariable String postId,
            @Valid @RequestBody PostCreateRequest request) {
        PostDetailResponse post = postService.update(postId, request);
        return ResponseEntity.ok(ApiResponse.success("Bài đăng đã được cập nhật", post));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String postId) {
        postService.delete(postId);
        return ResponseEntity.ok(ApiResponse.success("Bài đăng đã được xóa", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PostDetailResponse>>> search(PostFilterRequest filter) {
        PagedResponse<PostDetailResponse> posts = postService.search(filter);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<List<PostDetailResponse>>> getApproved() {
        List<PostDetailResponse> posts = postService.getApproved();
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<ApiResponse<List<PostDetailResponse>>> getMyPosts(
            @RequestParam(required = false) vn.agriconnect.API.model.enums.PostStatus status) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<PostDetailResponse> posts = postService.getBySeller(userId, status);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<PostDetailResponse>>> getBySeller(
            @PathVariable String sellerId,
            @RequestParam(required = false) vn.agriconnect.API.model.enums.PostStatus status) {
        List<PostDetailResponse> posts = postService.getBySeller(sellerId, status);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @PutMapping("/{postId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable String postId) {
        postService.approve(postId);
        return ResponseEntity.ok(ApiResponse.success("Bài đăng đã được duyệt", null));
    }

    @PutMapping("/{postId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable String postId,
            @RequestParam(required = false) String reason) {
        postService.reject(postId, reason);
        return ResponseEntity.ok(ApiResponse.success("Bài đăng đã bị từ chối", null));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<vn.agriconnect.API.dto.response.PostInteractionResponse>> toggleLike(@PathVariable String postId) {
        vn.agriconnect.API.dto.response.PostInteractionResponse result = postService.toggleLike(postId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{postId}/close")
    public ResponseEntity<ApiResponse<Void>> close(@PathVariable String postId) {
        postService.close(postId);
        return ResponseEntity.ok(ApiResponse.success("Bài đăng đã được đóng", null));
    }
}
