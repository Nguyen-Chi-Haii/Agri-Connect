package vn.agriconnect.API.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.agriconnect.API.model.Message;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    List<Message> findByConversationIdAndIsReadFalse(String conversationId);

    List<Message> findByConversationIdAndSenderIdNotAndIsReadFalse(String conversationId, String senderId);

    long countByConversationIdAndIsReadFalseAndSenderIdNot(String conversationId, String userId);
}
