package vn.agriconnect.API.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.model.AdminLog;
import vn.agriconnect.API.service.AdminService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<AdminLog>>> getLogs() {
        List<AdminLog> logs = adminService.getAllLogs();
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @PostMapping("/logs")
    public ResponseEntity<ApiResponse<Void>> createLog(
            @RequestParam String action,
            @RequestParam(required = false) String detail) {
        String adminId = SecurityContextHolder.getContext().getAuthentication().getName();
        adminService.logAction(adminId, action, detail);
        return ResponseEntity.ok(ApiResponse.success("Log created", null));
    }
}
