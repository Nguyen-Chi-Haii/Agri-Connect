package vn.agriconnect.API.event.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import vn.agriconnect.API.event.PostCreatedEvent;
import vn.agriconnect.API.event.UserRegisteredEvent;
import vn.agriconnect.API.service.NotificationService;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        notificationService.create(
                event.getUser().getId(),
                "Chào mừng!",
                "Chào mừng bạn đến với AgriConnect!"
        );
    }

    @EventListener
    public void handlePostCreated(PostCreatedEvent event) {
        // TODO: Notify relevant users about new post
    }
}
