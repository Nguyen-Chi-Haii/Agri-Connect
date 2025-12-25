package vn.agriconnect.API.service;

import vn.agriconnect.API.dto.request.post.PostCreateRequest;
import vn.agriconnect.API.dto.request.post.PostFilterRequest;
import vn.agriconnect.API.dto.response.PostDetailResponse;

import java.util.List;

public interface PostService {
    PostDetailResponse create(String sellerId, PostCreateRequest request);
    PostDetailResponse getById(String postId);
    PostDetailResponse update(String postId, PostCreateRequest request);
    void delete(String postId);
    List<PostDetailResponse> search(PostFilterRequest filter);
    List<PostDetailResponse> getBySeller(String sellerId);
    List<PostDetailResponse> getApproved();
    void approve(String postId);
    void reject(String postId, String reason);
    void incrementViewCount(String postId);
}
