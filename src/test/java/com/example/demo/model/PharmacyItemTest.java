package com.example.demo.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PharmacyItemTest {

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        PharmacyItem item1 = new PharmacyItem();
        item1.setId(id);
        item1.setName("Aspirin");
        item1.setManufacturer("Bayer");
        item1.setPrice(9.99);

        PharmacyItem item2 = new PharmacyItem();
        item2.setId(id);
        item2.setName("Aspirin");
        item2.setManufacturer("Bayer");
        item2.setPrice(9.99);

        // Test equals
        assertEquals(item1, item2);
        assertEquals(item1, item1);
        assertNotEquals(item1, null);
        assertNotEquals(item1, new Object());

        // Test hashCode
        assertEquals(item1.hashCode(), item2.hashCode());

        // Test with different values
        item2.setId(UUID.randomUUID());
        assertNotEquals(item1, item2);
        assertNotEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void testGettersAndSetters() {
        UUID id = UUID.randomUUID();
        String name = "Ibuprofen";
        String manufacturer = "Generic";
        double price = 5.99;

        PharmacyItem item = new PharmacyItem();
        item.setId(id);
        item.setName(name);
        item.setManufacturer(manufacturer);
        item.setPrice(price);

        assertEquals(id, item.getId());
        assertEquals(name, item.getName());
        assertEquals(manufacturer, item.getManufacturer());
        assertEquals(price, item.getPrice());
    }

    @Test
    void testToString() {
        PharmacyItem item = new PharmacyItem();
        UUID id = UUID.randomUUID();
        item.setId(id);
        item.setName("Test");
        item.setManufacturer("Test Corp");
        item.setPrice(10.0);

        String toString = item.toString();
        assertTrue(toString.contains(id.toString()));
        assertTrue(toString.contains("Test"));
        assertTrue(toString.contains("Test Corp"));
        assertTrue(toString.contains("10.0"));
    }
} 