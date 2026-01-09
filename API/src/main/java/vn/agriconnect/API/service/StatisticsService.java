package vn.agriconnect.API.service;

import vn.agriconnect.API.dto.response.StatisticsResponse;

public interface StatisticsService {
    StatisticsResponse getUserStatistics(String userId);
}
