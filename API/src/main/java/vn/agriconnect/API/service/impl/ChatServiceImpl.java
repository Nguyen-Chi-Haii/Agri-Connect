package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.dto.request.chat.SendMessageRequest;
import vn.agriconnect.API.model.Conversation;
import vn.agriconnect.API.model.Message;
import vn.agriconnect.API.model.User;
import vn.agriconnect.API.model.embedded.LastMessage;
import vn.agriconnect.API.model.enums.MessageType;
import vn.agriconnect.API.repository.ConversationRepository;
import vn.agriconnect.API.repository.MessageRepository;
import vn.agriconnect.API.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final vn.agriconnect.API.service.NotificationService notificationService;

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
        
        // --- Notification Trigger ---
        // Find the other participant in the conversation to notify
        conversationRepository.findById(request.getConversationId()).ifPresent(conv -> {
            String recipientId = conv.getParticipants().stream()
                    .filter(id -> !id.equals(senderId))
                    .findFirst()
                    .orElse(null);
            
            if (recipientId != null) {
                notificationService.create(
                    recipientId,
                    "Tin nhắn mới",
                    "Bạn có tin nhắn mới"
                );
            }

            // Update conversation's last message
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
        List<Message> unreadMessages = messageRepository.findByConversationIdAndSenderIdNotAndIsReadFalse(
                conversationId,
                userId);
        unreadMessages.forEach(m -> m.setRead(true));
        messageRepository.saveAll(unreadMessages);
    }

    @Override
    public List<vn.agriconnect.API.dto.response.ConversationResponse> getConversationsWithDetails(String userId) {
        List<Conversation> conversations = conversationRepository.findByParticipantsContaining(userId);

        return conversations.stream().map(conv -> {
            // Find the other user in the conversation
            String otherUserId = conv.getParticipants().stream()
                    .filter(p -> !p.equals(userId))
                    .findFirst()
                    .orElse(null);

            // Get other user's info from database
            String otherUserName = "Người dùng";
            String otherUserAvatar = null;

            if (otherUserId != null) {
                Optional<User> otherUser = userRepository.findById(otherUserId);
                if (otherUser.isPresent()) {
                    otherUserName = otherUser.get().getFullName();
                    otherUserAvatar = otherUser.get().getAvatar();
                }
            }

            // Count unread messages from other user
            List<Message> unreadMessages = messageRepository.findByConversationIdAndSenderIdNotAndIsReadFalse(
                    conv.getId(), userId);
            int unreadCount = unreadMessages.size();

            return vn.agriconnect.API.dto.response.ConversationResponse.builder()
                    .id(conv.getId())
                    .participants(conv.getParticipants())
                    .lastMessage(conv.getLastMessage())
                    .updatedAt(conv.getUpdatedAt())
                    .otherUserId(otherUserId)
                    .otherUserName(otherUserName)
                    .otherUserAvatar(otherUserAvatar)
                    .unreadCount(unreadCount)
                    .build();
        }).collect(java.util.stream.Collectors.toList());
    }
}
