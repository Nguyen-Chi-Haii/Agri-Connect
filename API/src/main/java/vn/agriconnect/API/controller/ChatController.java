package vn.agriconnect.API.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.request.chat.SendMessageRequest;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.model.Conversation;
import vn.agriconnect.API.model.Message;
import vn.agriconnect.API.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<Conversation>>> getConversations() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Conversation> conversations = chatService.getConversations(userId);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @PostMapping("/conversations/{otherUserId}")
    public ResponseEntity<ApiResponse<Conversation>> createConversation(@PathVariable String otherUserId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Conversation conversation = chatService.getOrCreateConversation(userId, otherUserId);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<Message>>> getMessages(@PathVariable String conversationId) {
        List<Message> messages = chatService.getMessages(conversationId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<Message>> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Message message = chatService.sendMessage(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Message sent", message));
    }

    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String conversationId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        chatService.markAsRead(conversationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }
}
