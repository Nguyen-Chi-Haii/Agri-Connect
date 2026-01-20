package vn.agriconnect.API.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.agriconnect.API.model.embedded.LastMessage;

import java.time.Instant;
import java.util.List;

/**
 * Conversation Response DTO with participant details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String id;
    private List<String> participants;
    private LastMessage lastMessage;
    private Instant updatedAt;

    // Additional fields for client display
    private String otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private int unreadCount;
}
