package vn.agriconnect.API.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import vn.agriconnect.API.model.Post;

@Getter
public class PostCreatedEvent extends ApplicationEvent {
    
    private final Post post;

    public PostCreatedEvent(Object source, Post post) {
        super(source);
        this.post = post;
    }
}
