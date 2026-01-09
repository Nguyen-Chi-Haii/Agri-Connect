package vn.agriconnect.API.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.dto.response.StatisticsResponse;
import vn.agriconnect.API.service.StatisticsService;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getSummary() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        StatisticsResponse stats = statisticsService.getUserStatistics(userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
