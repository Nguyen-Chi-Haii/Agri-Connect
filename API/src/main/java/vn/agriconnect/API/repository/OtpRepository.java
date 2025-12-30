package vn.agriconnect.API.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.agriconnect.API.model.Otp;

import java.util.Optional;

@Repository
public interface OtpRepository extends MongoRepository<Otp, String> {
    
    Optional<Otp> findByPhoneAndCodeAndUsedFalse(String phone, String code);
    
    void deleteByPhone(String phone);
}
