package vn.agriconnect.API.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        // TODO: Get current user ID from SecurityContext
        List<Conversation> conversations = chatService.getConversations("currentUserId");
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @PostMapping("/conversations/{userId}")
    public ResponseEntity<ApiResponse<Conversation>> createConversation(@PathVariable String userId) {
        // TODO: Get current user ID from SecurityContext
        Conversation conversation = chatService.getOrCreateConversation("currentUserId", userId);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<Message>>> getMessages(@PathVariable String conversationId) {
        List<Message> messages = chatService.getMessages(conversationId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<Message>> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        // TODO: Get current user ID from SecurityContext
        Message message = chatService.sendMessage("currentUserId", request);
        return ResponseEntity.ok(ApiResponse.success("Message sent", message));
    }

    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String conversationId) {
        // TODO: Get current user ID from SecurityContext
        chatService.markAsRead(conversationId, "currentUserId");
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }
}
