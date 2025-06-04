package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import lombok.Data;
import java.util.UUID;

@Data
@Document(collection = "pharmacy_items")
@CompoundIndexes({
    @CompoundIndex(
        name = "manufacturer_name_price_unique", 
        def = "{'manufacturer': 1, 'name': 1, 'price': 1}", 
        unique = true
    )
})
public class PharmacyItem {
    @Id
    private UUID id;
    private String manufacturer;
    private String name;
    private double price;
} 