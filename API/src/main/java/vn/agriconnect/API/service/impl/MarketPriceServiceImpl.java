package vn.agriconnect.API.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.agriconnect.API.model.MarketPrice;
import vn.agriconnect.API.repository.MarketPriceRepository;
import vn.agriconnect.API.service.MarketPriceService;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MarketPriceServiceImpl implements MarketPriceService {

    private final MarketPriceRepository marketPriceRepository;

    @Override
    public MarketPrice create(MarketPrice marketPrice) {
        return marketPriceRepository.save(marketPrice);
    }

    @Override
    public List<MarketPrice> getByCategory(String categoryId) {
        return marketPriceRepository.findByCategoryIdOrderByDateDesc(categoryId);
    }

    @Override
    public List<MarketPrice> getAll() {
        return marketPriceRepository.findAll();
    }
}
