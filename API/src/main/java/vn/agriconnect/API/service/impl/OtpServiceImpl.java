package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.agriconnect.API.dto.request.auth.SendOtpRequest;
import vn.agriconnect.API.dto.request.auth.VerifyOtpRequest;
import vn.agriconnect.API.dto.response.OtpResponse;
import vn.agriconnect.API.exception.BadRequestException;
import vn.agriconnect.API.model.Otp;
import vn.agriconnect.API.repository.OtpRepository;
import vn.agriconnect.API.repository.UserRepository;
import vn.agriconnect.API.service.OtpService;
import vn.agriconnect.API.service.SmsService;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final SmsService smsService;

    @Value("${otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${otp.length:6}")
    private int otpLength;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public OtpResponse sendOtp(SendOtpRequest request) {
        String phone = request.getPhone();
        
        // Xóa OTP cũ của số điện thoại này
        otpRepository.deleteByPhone(phone);
        
        // Tạo mã OTP mới
        String code = generateOtpCode();
        
        // Lưu OTP vào database
        Otp otp = new Otp();
        otp.setPhone(phone);
        otp.setCode(code);
        otp.setCreatedAt(Instant.now());
        otp.setExpiresAt(Instant.now().plus(otpExpiryMinutes, ChronoUnit.MINUTES));
        otp.setUsed(false);
        otpRepository.save(otp);
        
        // Gửi SMS (hoặc notification trong dev mode)
        String message = String.format("Mã OTP của bạn là: %s. Mã có hiệu lực trong %d phút.", 
                code, otpExpiryMinutes);
        smsService.sendSms(phone, message);
        
        log.info("OTP sent to phone: {}", phone);
        
        return OtpResponse.builder()
                .message("OTP đã được gửi đến số điện thoại của bạn")
                .verified(false)
                .expiresInSeconds(otpExpiryMinutes * 60)
                .build();
    }

    @Override
    @Transactional
    public OtpResponse verifyOtp(VerifyOtpRequest request) {
        String phone = request.getPhone();
        String code = request.getOtp();
        
        // Tìm OTP hợp lệ
        Otp otp = otpRepository.findByPhoneAndCodeAndUsedFalse(phone, code)
                .orElseThrow(() -> new BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn"));
        
        // Kiểm tra hết hạn
        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Mã OTP đã hết hạn");
        }
        
        // Đánh dấu OTP đã sử dụng
        otp.setUsed(true);
        otpRepository.save(otp);
        
        // Cập nhật user.phoneVerified = true
        userRepository.findByPhone(phone).ifPresent(user -> {
            user.setPhoneVerified(true);
            userRepository.save(user);
            log.info("Phone verified for user: {}", user.getId());
        });
        
        return OtpResponse.builder()
                .message("Xác thực số điện thoại thành công")
                .verified(true)
                .expiresInSeconds(0)
                .build();
    }

    private String generateOtpCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
