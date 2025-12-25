package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.dto.request.chat.SendMessageRequest;
import vn.agriconnect.API.model.Conversation;
import vn.agriconnect.API.model.Message;
import vn.agriconnect.API.repository.ConversationRepository;
import vn.agriconnect.API.repository.MessageRepository;
import vn.agriconnect.API.service.ChatService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    public Conversation getOrCreateConversation(String userId1, String userId2) {
        // TODO: Implement
        return null;
    }

    @Override
    public List<Conversation> getConversations(String userId) {
        return conversationRepository.findByParticipantsContaining(userId);
    }

    @Override
    public Message sendMessage(String senderId, SendMessageRequest request) {
        // TODO: Implement
        return null;
    }

    @Override
    public List<Message> getMessages(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Override
    public void markAsRead(String conversationId, String userId) {
        // TODO: Implement
    }
}
