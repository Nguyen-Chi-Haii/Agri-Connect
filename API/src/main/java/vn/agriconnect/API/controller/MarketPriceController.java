package vn.agriconnect.API.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.model.MarketPrice;
import vn.agriconnect.API.service.MarketPriceService;

import java.util.List;

@RestController
@RequestMapping("/api/market-prices")
@RequiredArgsConstructor
public class MarketPriceController {

    private final MarketPriceService marketPriceService;

    @PostMapping
    public ResponseEntity<ApiResponse<MarketPrice>> create(@RequestBody MarketPrice marketPrice) {
        MarketPrice created = marketPriceService.create(marketPrice);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm giá thị trường", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MarketPrice>>> getAll() {
        List<MarketPrice> prices = marketPriceService.getAll();
        return ResponseEntity.ok(ApiResponse.success(prices));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<MarketPrice>>> getByCategory(@PathVariable String categoryId) {
        List<MarketPrice> prices = marketPriceService.getByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(prices));
    }
}
