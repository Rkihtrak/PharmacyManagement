package com.example.demo.controller;

import com.example.demo.model.PharmacyItem;
import com.example.demo.repository.PharmacyItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PharmacyItemController.class)
class PharmacyItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PharmacyItemRepository pharmacyItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private PharmacyItem testItem;
    private final UUID testId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @BeforeEach
    void setUp() {
        testItem = new PharmacyItem();
        testItem.setId(testId);
        testItem.setName("Test Medicine");
        testItem.setManufacturer("Test Corp");
        testItem.setPrice(9.99);
    }

    @Test
    void getAllItems_ShouldReturnAllItems() throws Exception {
        List<PharmacyItem> items = Arrays.asList(testItem);
        when(pharmacyItemRepository.findAll()).thenReturn(items);

        mockMvc.perform(get("/api/pharmacy-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Medicine"))
                .andExpect(jsonPath("$[0].manufacturer").value("Test Corp"))
                .andExpect(jsonPath("$[0].price").value(9.99));
    }

    @Test
    void getAllItems_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        when(pharmacyItemRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/pharmacy-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createItems_ShouldCreateItems() throws Exception {
        List<PharmacyItem> items = Arrays.asList(testItem);
        when(pharmacyItemRepository.saveAll(any())).thenReturn(items);

        mockMvc.perform(post("/api/pharmacy-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Medicine"));
    }

    @Test
    void createItems_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        List<Map<String, Object>> invalidItems = Arrays.asList(
            Map.of("name", "", "manufacturer", "", "price", -1)
        );

        mockMvc.perform(post("/api/pharmacy-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItems)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItem_WithValidId_ShouldReturnItem() throws Exception {
        when(pharmacyItemRepository.findById(testId)).thenReturn(Optional.of(testItem));

        mockMvc.perform(get("/api/pharmacy-items/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Medicine"));
    }

    @Test
    void getItem_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/pharmacy-items/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItem_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(pharmacyItemRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pharmacy-items/{id}", testId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_WithValidData_ShouldUpdateItem() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Medicine");
        updates.put("price", 19.99);

        when(pharmacyItemRepository.findById(testId)).thenReturn(Optional.of(testItem));
        when(pharmacyItemRepository.save(any(PharmacyItem.class))).thenReturn(testItem);

        mockMvc.perform(patch("/api/pharmacy-items/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Medicine");

        mockMvc.perform(patch("/api/pharmacy-items/invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Updated Medicine");

        when(pharmacyItemRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/pharmacy-items/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_WithInvalidPrice_ShouldHandleException() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("price", "invalid-price");

        when(pharmacyItemRepository.findById(testId)).thenReturn(Optional.of(testItem));

        mockMvc.perform(patch("/api/pharmacy-items/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItems_ByName_ShouldReturnMatchingItems() throws Exception {
        when(pharmacyItemRepository.findByNameContainingIgnoreCase(anyString()))
                .thenReturn(Arrays.asList(testItem));

        mockMvc.perform(get("/api/pharmacy-items/search")
                .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Medicine"));
    }

    @Test
    void searchItems_ByManufacturer_ShouldReturnMatchingItems() throws Exception {
        when(pharmacyItemRepository.findByManufacturerContainingIgnoreCase(anyString()))
                .thenReturn(Arrays.asList(testItem));

        mockMvc.perform(get("/api/pharmacy-items/search")
                .param("manufacturer", "Corp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].manufacturer").value("Test Corp"));
    }

    @Test
    void searchItems_ByPriceRange_ShouldReturnMatchingItems() throws Exception {
        when(pharmacyItemRepository.findByPriceBetween(anyDouble(), anyDouble()))
                .thenReturn(Arrays.asList(testItem));

        mockMvc.perform(get("/api/pharmacy-items/search")
                .param("minPrice", "5.0")
                .param("maxPrice", "15.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(9.99));
    }

    @Test
    void searchItems_ByMinPriceOnly_ShouldReturnMatchingItems() throws Exception {
        when(pharmacyItemRepository.findByPriceGreaterThanOrEqual(anyDouble()))
                .thenReturn(Arrays.asList(testItem));

        mockMvc.perform(get("/api/pharmacy-items/search")
                .param("minPrice", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(9.99));
    }

    @Test
    void searchItems_ByMaxPriceOnly_ShouldReturnMatchingItems() throws Exception {
        when(pharmacyItemRepository.findByPriceLessThanOrEqual(anyDouble()))
                .thenReturn(Arrays.asList(testItem));

        mockMvc.perform(get("/api/pharmacy-items/search")
                .param("maxPrice", "15.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(9.99));
    }

    @Test
    void searchItems_ByExactPrice_ShouldReturnMatchingItems() throws Exception {
        when(pharmacyItemRepository.findByPrice(anyDouble()))
                .thenReturn(Arrays.asList(testItem));

        mockMvc.perform(get("/api/pharmacy-items/search")
                .param("price", "9.99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(9.99));
    }

    @Test
    void searchItems_WithNoParams_ShouldReturnAllItems() throws Exception {
        when(pharmacyItemRepository.findAll())
                .thenReturn(Arrays.asList(testItem));

        mockMvc.perform(get("/api/pharmacy-items/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void deleteItems_ById_ShouldDeleteItem() throws Exception {
        when(pharmacyItemRepository.existsById(testId)).thenReturn(true);
        doNothing().when(pharmacyItemRepository).deleteById(any(UUID.class));

        mockMvc.perform(delete("/api/pharmacy-items")
                .param("id", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete operation completed"))
                .andExpect(jsonPath("$.deletedCount").value(1));

        verify(pharmacyItemRepository).deleteById(testId);
    }

    @Test
    void deleteItems_ByIdNonExistent_ShouldReturnZeroCount() throws Exception {
        when(pharmacyItemRepository.existsById(testId)).thenReturn(false);

        mockMvc.perform(delete("/api/pharmacy-items")
                .param("id", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete operation completed"))
                .andExpect(jsonPath("$.deletedCount").value(0));
    }

    @Test
    void deleteItems_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/pharmacy-items")
                .param("id", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid UUID format: invalid-uuid"));
    }

    @Test
    void deleteItems_ByManufacturer_ShouldDeleteItems() throws Exception {
        when(pharmacyItemRepository.countByManufacturer("Test Corp")).thenReturn(2L);
        doNothing().when(pharmacyItemRepository).deleteByManufacturer(anyString());

        mockMvc.perform(delete("/api/pharmacy-items")
                .param("manufacturer", "Test Corp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete operation completed"))
                .andExpect(jsonPath("$.deletedCount").value(2));

        verify(pharmacyItemRepository).deleteByManufacturer("Test Corp");
    }

    @Test
    void deleteItems_ByName_ShouldDeleteItems() throws Exception {
        when(pharmacyItemRepository.countByName("Test Medicine")).thenReturn(1L);
        doNothing().when(pharmacyItemRepository).deleteByName(anyString());

        mockMvc.perform(delete("/api/pharmacy-items")
                .param("name", "Test Medicine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete operation completed"))
                .andExpect(jsonPath("$.deletedCount").value(1));

        verify(pharmacyItemRepository).deleteByName("Test Medicine");
    }

    @Test
    void deleteItems_ByPriceRange_ShouldDeleteItems() throws Exception {
        when(pharmacyItemRepository.countByPriceBetween(5.0, 15.0)).thenReturn(3L);
        doNothing().when(pharmacyItemRepository).deleteByPriceBetween(anyDouble(), anyDouble());

        mockMvc.perform(delete("/api/pharmacy-items")
                .param("minPrice", "5.0")
                .param("maxPrice", "15.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete operation completed"))
                .andExpect(jsonPath("$.deletedCount").value(3));

        verify(pharmacyItemRepository).deleteByPriceBetween(5.0, 15.0);
    }

    @Test
    void deleteItems_ByMinPriceOnly_ShouldDeleteItems() throws Exception {
        when(pharmacyItemRepository.countByPriceGreaterThanEqual(5.0)).thenReturn(2L);
        doNothing().when(pharmacyItemRepository).deleteByPriceGreaterThanEqual(anyDouble());

        mockMvc.perform(delete("/api/pharmacy-items")
                .param("minPrice", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete operation completed"))
                .andExpect(jsonPath("$.deletedCount").value(2));

        verify(pharmacyItemRepository).deleteByPriceGreaterThanEqual(5.0);
    }

    @Test
    void deleteItems_ByMaxPriceOnly_ShouldDeleteItems() throws Exception {
        when(pharmacyItemRepository.countByPriceLessThanEqual(15.0)).thenReturn(4L);
        doNothing().when(pharmacyItemRepository).deleteByPriceLessThanEqual(anyDouble());

        mockMvc.perform(delete("/api/pharmacy-items")
                .param("maxPrice", "15.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete operation completed"))
                .andExpect(jsonPath("$.deletedCount").value(4));

        verify(pharmacyItemRepository).deleteByPriceLessThanEqual(15.0);
    }

    @Test
    void deleteItems_WithNoParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/pharmacy-items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItems_WithDuplicateItem_ShouldReturnBadRequest() throws Exception {
        List<PharmacyItem> items = Arrays.asList(testItem);
        when(pharmacyItemRepository.findByManufacturerAndNameAndPrice(
            testItem.getManufacturer(), testItem.getName(), testItem.getPrice()))
            .thenReturn(Optional.of(testItem));

        mockMvc.perform(post("/api/pharmacy-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("An item with the same manufacturer, name, and price already exists"))
                .andExpect(jsonPath("$.conflicting_item.id").value(testId.toString()));
    }

    @Test
    void updateItem_ToCreateDuplicate_ShouldReturnBadRequest() throws Exception {
        // Create a second item that would be a duplicate after update
        PharmacyItem existingItem = new PharmacyItem();
        UUID existingId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        existingItem.setId(existingId);
        existingItem.setName("Test Medicine");
        existingItem.setManufacturer("Test Corp");
        existingItem.setPrice(9.99);

        // Setup the item to update
        PharmacyItem itemToUpdate = new PharmacyItem();
        itemToUpdate.setId(testId);
        itemToUpdate.setName("Different Medicine");
        itemToUpdate.setManufacturer("Different Corp");
        itemToUpdate.setPrice(19.99);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Test Medicine");
        updates.put("manufacturer", "Test Corp");
        updates.put("price", 9.99);

        when(pharmacyItemRepository.findById(testId)).thenReturn(Optional.of(itemToUpdate));
        when(pharmacyItemRepository.findByManufacturerAndNameAndPriceAndIdNot(
            "Test Corp", "Test Medicine", 9.99, testId))
            .thenReturn(Optional.of(existingItem));

        mockMvc.perform(patch("/api/pharmacy-items/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("An item with the same manufacturer, name, and price already exists"))
                .andExpect(jsonPath("$.conflicting_item.id").value(existingId.toString()));
    }

    @Test
    void updateItem_WithSameValues_ShouldSucceed() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", testItem.getName());
        updates.put("manufacturer", testItem.getManufacturer());
        updates.put("price", testItem.getPrice());

        when(pharmacyItemRepository.findById(testId)).thenReturn(Optional.of(testItem));
        when(pharmacyItemRepository.save(any(PharmacyItem.class))).thenReturn(testItem);

        mockMvc.perform(patch("/api/pharmacy-items/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_WithPartialUpdate_ShouldNotCheckDuplicates() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("price", 29.99); // Only updating price

        when(pharmacyItemRepository.findById(testId)).thenReturn(Optional.of(testItem));
        when(pharmacyItemRepository.save(any(PharmacyItem.class))).thenReturn(testItem);

        // No need to mock findByManufacturerAndNameAndPriceAndIdNot because it shouldn't be called

        mockMvc.perform(patch("/api/pharmacy-items/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk());

        verify(pharmacyItemRepository, never()).findByManufacturerAndNameAndPriceAndIdNot(
            anyString(), anyString(), anyDouble(), any(UUID.class));
    }
} 