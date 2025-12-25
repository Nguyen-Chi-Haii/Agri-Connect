package vn.agriconnect.API.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import vn.agriconnect.API.model.User;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    
    private final User user;

    public UserRegisteredEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
