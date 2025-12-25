package vn.agriconnect.API.service;

import vn.agriconnect.API.model.AdminLog;

import java.util.List;
import java.util.Map;

public interface AdminService {
    void logAction(String adminId, String action, String detail);
    List<AdminLog> getLogs(String adminId);
    Map<String, Object> getDashboardStats();
}
