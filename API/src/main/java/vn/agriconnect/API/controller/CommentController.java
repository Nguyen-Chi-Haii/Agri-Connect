package vn.agriconnect.API.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.dto.response.PagedResponse;
import vn.agriconnect.API.model.Comment;
import vn.agriconnect.API.service.CommentService;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PagedResponse<Comment>>> getComments(
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentsByPost(postId, page, size)));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<Comment>> addComment(
            @PathVariable String postId,
            @RequestBody Map<String, String> payload
    ) {
        String content = payload.get("content");
        return ResponseEntity.ok(ApiResponse.success(commentService.addComment(postId, content)));
    }
}
