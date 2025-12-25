package vn.agriconnect.API.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.model.AdminLog;
import vn.agriconnect.API.service.AdminService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<AdminLog>>> getLogs() {
        // TODO: Get current admin ID from SecurityContext
        List<AdminLog> logs = adminService.getLogs("currentAdminId");
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
