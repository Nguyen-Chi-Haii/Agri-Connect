package vn.agriconnect.API.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.agriconnect.API.model.AdminLog;

import java.util.List;

@Repository
public interface AdminLogRepository extends MongoRepository<AdminLog, String> {
    List<AdminLog> findByAdminIdOrderByTimestampDesc(String adminId);
    List<AdminLog> findAllByOrderByTimestampDesc();
}
