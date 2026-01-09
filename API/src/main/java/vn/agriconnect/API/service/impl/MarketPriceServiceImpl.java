package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.model.Category;
import vn.agriconnect.API.model.MarketPrice;
import vn.agriconnect.API.repository.CategoryRepository;
import vn.agriconnect.API.repository.MarketPriceRepository;
import vn.agriconnect.API.service.MarketPriceService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MarketPriceServiceImpl implements MarketPriceService {

    private final MarketPriceRepository marketPriceRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public MarketPrice create(MarketPrice marketPrice) {
        return marketPriceRepository.save(marketPrice);
    }

    @Override
    public List<MarketPrice> getByCategory(String categoryId) {
        List<MarketPrice> prices = marketPriceRepository.findByCategoryIdOrderByDateDesc(categoryId);
        populateCategoryNames(prices);
        return prices;
    }

    @Override
    public List<MarketPrice> getAll() {
        List<MarketPrice> prices = marketPriceRepository.findAll();
        populateCategoryNames(prices);
        return prices;
    }

    private void populateCategoryNames(List<MarketPrice> prices) {
        // Tạo map categoryId -> categoryName
        Map<String, String> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        for (MarketPrice mp : prices) {
            if (mp.getCategoryId() != null && categoryMap.containsKey(mp.getCategoryId())) {
                mp.setCategoryName(categoryMap.get(mp.getCategoryId()));
            }
            // Nếu không có productName, dùng categoryName
            if (mp.getProductName() == null || mp.getProductName().isEmpty()) {
                mp.setProductName(mp.getCategoryName());
            }
        }
    }
}
