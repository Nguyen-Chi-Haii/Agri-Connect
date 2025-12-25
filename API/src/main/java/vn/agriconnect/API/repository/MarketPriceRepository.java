package vn.agriconnect.API.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.agriconnect.API.model.MarketPrice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketPriceRepository extends MongoRepository<MarketPrice, String> {
    List<MarketPrice> findByCategoryId(String categoryId);
    List<MarketPrice> findByCategoryIdOrderByDateDesc(String categoryId);
    Optional<MarketPrice> findByCategoryIdAndDate(String categoryId, LocalDate date);
}
