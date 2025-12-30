package vn.agriconnect.API.service;

import vn.agriconnect.API.dto.request.auth.SendOtpRequest;
import vn.agriconnect.API.dto.request.auth.VerifyOtpRequest;
import vn.agriconnect.API.dto.response.OtpResponse;

public interface OtpService {
    OtpResponse sendOtp(SendOtpRequest request);
    OtpResponse verifyOtp(VerifyOtpRequest request);
}
