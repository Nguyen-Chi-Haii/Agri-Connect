package vn.agriconnect.API.service;

import vn.agriconnect.API.dto.response.PagedResponse;
import vn.agriconnect.API.model.Comment;

public interface CommentService {
    Comment addComment(String postId, String content);
    PagedResponse<Comment> getCommentsByPost(String postId, int page, int size);
    void deleteComment(String commentId);
}
