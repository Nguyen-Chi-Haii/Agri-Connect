package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.model.AdminLog;
import vn.agriconnect.API.repository.AdminLogRepository;
import vn.agriconnect.API.service.AdminService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminLogRepository adminLogRepository;

    @Override
    public void logAction(String adminId, String action, String detail) {
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction(action);
        log.setDetail(detail);
        log.setTimestamp(Instant.now());
        adminLogRepository.save(log);
    }

    @Override
    public List<AdminLog> getLogs(String adminId) {
        return adminLogRepository.findByAdminIdOrderByTimestampDesc(adminId);
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        // TODO: Implement dashboard statistics
        return new HashMap<>();
    }
}
