package vn.agriconnect.API.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import vn.agriconnect.API.event.UserRegisteredEvent;

@Slf4j
@Component
public class EmailEventListener {

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Sending welcome SMS to: {}", event.getUser().getPhone());
    }
}
