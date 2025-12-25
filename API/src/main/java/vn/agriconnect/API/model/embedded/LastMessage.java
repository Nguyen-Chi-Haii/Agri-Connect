package vn.agriconnect.API.model.embedded;

import lombok.Data;
import vn.agriconnect.API.model.enums.MessageType;

import java.time.Instant;

/**
 * Last Message Summary - Embedded Document for Conversation
 */
@Data
public class LastMessage {
    private String content;
    private MessageType type;
    private Instant time;
    private String senderId;
}
