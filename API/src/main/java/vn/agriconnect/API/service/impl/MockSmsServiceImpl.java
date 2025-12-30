package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.repository.UserRepository;
import vn.agriconnect.API.service.NotificationService;
import vn.agriconnect.API.service.SmsService;

/**
 * Mock SMS Service - Dùng trong giai đoạn development
 * Thay vì gửi SMS thật, tạo notification trong app để test
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockSmsServiceImpl implements SmsService {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    public void sendSms(String phone, String message) {
        // Log ra console
        log.info("📱 [MOCK SMS] To: {} | Message: {}", phone, message);

        // Tạo notification trong app giả lập tin nhắn SMS
        userRepository.findByPhone(phone).ifPresent(user -> {
            notificationService.create(
                    user.getId(),
                    "📱 Mã OTP (Dev Mode)",
                    message
            );
            log.info("📬 Created in-app notification for user: {}", user.getId());
        });
    }
}
