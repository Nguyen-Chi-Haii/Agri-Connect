package vn.agriconnect.API.service;

import vn.agriconnect.API.model.MarketPrice;

import java.util.List;

public interface MarketPriceService {
    MarketPrice create(MarketPrice marketPrice);
    List<MarketPrice> getByCategory(String categoryId);
    List<MarketPrice> getAll();
}
