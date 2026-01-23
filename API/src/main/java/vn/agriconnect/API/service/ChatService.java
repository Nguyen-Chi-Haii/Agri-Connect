package vn.agriconnect.API.service;

import vn.agriconnect.API.dto.request.chat.SendMessageRequest;
import vn.agriconnect.API.model.Conversation;
import vn.agriconnect.API.model.Message;

import java.util.List;

public interface ChatService {
    Conversation getOrCreateConversation(String userId1, String userId2);

    List<Conversation> getConversations(String userId);

    List<vn.agriconnect.API.dto.response.ConversationResponse> getConversationsWithDetails(String userId);

    Message sendMessage(String senderId, SendMessageRequest request);

    List<Message> getMessages(String conversationId);

    void markAsRead(String conversationId, String userId);

    long countUnreadConversations(String userId);
}
