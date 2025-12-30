package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.dto.request.chat.SendMessageRequest;
import vn.agriconnect.API.model.Conversation;
import vn.agriconnect.API.model.Message;
import vn.agriconnect.API.model.embedded.LastMessage;
import vn.agriconnect.API.model.enums.MessageType;
import vn.agriconnect.API.repository.ConversationRepository;
import vn.agriconnect.API.repository.MessageRepository;
import vn.agriconnect.API.service.ChatService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    public Conversation getOrCreateConversation(String userId1, String userId2) {
        // Check if conversation already exists between these two users
        List<Conversation> existing = conversationRepository.findByParticipantsContaining(userId1);
        
        Optional<Conversation> found = existing.stream()
                .filter(c -> c.getParticipants().contains(userId2))
                .findFirst();
        
        if (found.isPresent()) {
            return found.get();
        }
        
        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setParticipants(Arrays.asList(userId1, userId2));
        conversation.setUpdatedAt(Instant.now());
        
        return conversationRepository.save(conversation);
    }

    @Override
    public List<Conversation> getConversations(String userId) {
        return conversationRepository.findByParticipantsContaining(userId);
    }

    @Override
    public Message sendMessage(String senderId, SendMessageRequest request) {
        // Create and save message
        Message message = new Message();
        message.setConversationId(request.getConversationId());
        message.setSenderId(senderId);
        message.setContent(request.getContent());
        message.setType(request.getType() != null ? request.getType() : MessageType.TEXT);
        message.setRead(false);
        
        Message savedMessage = messageRepository.save(message);
        
        // Update conversation's last message
        conversationRepository.findById(request.getConversationId()).ifPresent(conv -> {
            LastMessage lastMessage = new LastMessage();
            lastMessage.setContent(request.getContent());
            lastMessage.setType(savedMessage.getType());
            lastMessage.setTime(savedMessage.getCreatedAt());
            lastMessage.setSenderId(senderId);
            
            conv.setLastMessage(lastMessage);
            conv.setUpdatedAt(Instant.now());
            conversationRepository.save(conv);
        });
        
        return savedMessage;
    }

    @Override
    public List<Message> getMessages(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Override
    public void markAsRead(String conversationId, String userId) {
        // Mark all messages in the conversation as read (except those sent by the user)
        List<Message> unreadMessages = messageRepository.findByConversationIdAndSenderIdNotAndReadFalse(conversationId, userId);
        unreadMessages.forEach(m -> m.setRead(true));
        messageRepository.saveAll(unreadMessages);
    }
}
