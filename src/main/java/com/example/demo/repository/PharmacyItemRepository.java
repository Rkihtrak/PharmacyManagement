package com.example.demo.repository;

import com.example.demo.model.PharmacyItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface PharmacyItemRepository extends MongoRepository<PharmacyItem, UUID> {
    List<PharmacyItem> findByManufacturer(String manufacturer);
    List<PharmacyItem> findByName(String name);
    List<PharmacyItem> findByNameContainingIgnoreCase(String name);
    List<PharmacyItem> findByManufacturerContainingIgnoreCase(String manufacturer);
    List<PharmacyItem> findByPrice(double price);
    
    @Query("{ 'manufacturer': ?0, 'name': ?1, 'price': ?2, '_id': { $ne: ?3 } }")
    Optional<PharmacyItem> findByManufacturerAndNameAndPriceAndIdNot(String manufacturer, String name, double price, UUID id);
    
    @Query("{ 'manufacturer': ?0, 'name': ?1, 'price': ?2 }")
    Optional<PharmacyItem> findByManufacturerAndNameAndPrice(String manufacturer, String name, double price);
    
    @Query("{ 'price' : { $lte: ?0 } }")
    List<PharmacyItem> findByPriceLessThanOrEqual(double price);
    
    @Query("{ 'price' : { $gte: ?0 } }")
    List<PharmacyItem> findByPriceGreaterThanOrEqual(double price);
    
    @Query("{ 'price' : { $gte: ?0, $lte: ?1 } }")
    List<PharmacyItem> findByPriceBetween(double minPrice, double maxPrice);
    
    @Query(value = "{ 'manufacturer': ?0 }", count = true)
    long countByManufacturer(String manufacturer);
    
    @Query(value = "{ 'name': ?0 }", count = true)
    long countByName(String name);
    
    @Query(value = "{ 'price' : { $lt: ?0 } }", count = true)
    long countByPriceLessThan(double price);
    
    @Query(value = "{ 'price' : { $gt: ?0 } }", count = true)
    long countByPriceGreaterThan(double price);
    
    @Query(value = "{ 'price' : { $gte: ?0, $lte: ?1 } }", count = true)
    long countByPriceBetween(double minPrice, double maxPrice);
    
    void deleteByManufacturer(String manufacturer);
    void deleteByName(String name);
    
    @Query("{ 'price' : { $lt: ?0 } }")
    void deleteByPriceLessThan(double price);
    
    @Query("{ 'price' : { $gt: ?0 } }")
    void deleteByPriceGreaterThan(double price);
    
    @Query(delete = true, value = "{ 'price' : { $gte: ?0, $lte: ?1 } }")
    void deleteByPriceBetween(double minPrice, double maxPrice);
    
    @Query(delete = true, value = "{ 'price' : { $gte: ?0 } }")
    void deleteByPriceGreaterThanEqual(double price);
    
    @Query(delete = true, value = "{ 'price' : { $lte: ?0 } }")
    void deleteByPriceLessThanEqual(double price);
    
    @Query(value = "{ 'price' : { $gte: ?0 } }", count = true)
    long countByPriceGreaterThanEqual(double price);
    
    @Query(value = "{ 'price' : { $lte: ?0 } }", count = true)
    long countByPriceLessThanEqual(double price);
} 


/*

 if (name != null) {
            return pharmacyItemRepository.findByNameContainingIgnoreCase(name);
        }
        if (manufacturer != null) {
            return pharmacyItemRepository.findByManufacturerContainingIgnoreCase(manufacturer);
        }
        if (minPrice != null && maxPrice != null) {
            return pharmacyItemRepository.findByPriceBetween(minPrice, maxPrice);
        }
        if (minPrice != null) {
            return pharmacyItemRepository.findByPriceGreaterThanOrEqual(minPrice);
        }
        if (maxPrice != null) {
            return pharmacyItemRepository.findByPriceLessThanOrEqual(maxPrice);
        }
        if (exactPrice != null) {
            return pharmacyItemRepository.findByPrice(exactPrice);
        }

        return pharmacyItemRepository.findAll();


*/