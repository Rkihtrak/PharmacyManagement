package com.example.demo.controller;

import com.example.demo.model.PharmacyItem;
import com.example.demo.repository.PharmacyItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

@RestController
@RequestMapping("/api/pharmacy-items")
@Tag(name = "Pharmacy Items", description = "Pharmacy Items Management APIs")
public class PharmacyItemController {

    @Autowired
    private PharmacyItemRepository pharmacyItemRepository;

    @Operation(
        summary = "Get all pharmacy items",
        description = "Retrieves a list of all pharmacy items from the database"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved all items",
        content = @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = PharmacyItem.class)),
            examples = @ExampleObject(
                value = """
                [
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "manufacturer": "PharmaCorp",
                        "name": "Aspirin 100mg",
                        "price": 9.99
                    },
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174001",
                        "manufacturer": "MediCo",
                        "name": "Ibuprofen 200mg",
                        "price": 12.99
                    }
                ]
                """
            )
        )
    )
    @GetMapping
    public List<PharmacyItem> getAllItems() {
        return pharmacyItemRepository.findAll();
    }

    @Operation(
        summary = "Create pharmacy items",
        description = "Creates pharmacy items as a list"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Items created successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = PharmacyItem.class)),
                examples = @ExampleObject(
                    value = """
                    [
                        {
                            "manufacturer": "PharmaCorp",
                            "name": "Aspirin 100mg",
                            "price": 9.99
                        },
                        {
                            "manufacturer": "MediCo",
                            "name": "Ibuprofen 200mg",
                            "price": 12.99
                        }
                    ]
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<?> createItems(@RequestBody List<PharmacyItem> request) {
        // Validate items
        for (PharmacyItem item : request) {
            if (item.getName() == null || item.getName().trim().isEmpty() ||
                item.getManufacturer() == null || item.getManufacturer().trim().isEmpty() ||
                item.getPrice() < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid item data"));
            }

            // Check for duplicates
            Optional<PharmacyItem> existingItem = pharmacyItemRepository.findByManufacturerAndNameAndPrice(
                item.getManufacturer(), item.getName(), item.getPrice());
            if (existingItem.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "An item with the same manufacturer, name, and price already exists",
                    "conflicting_item", existingItem.get()
                ));
            }
        }
        return ResponseEntity.ok(pharmacyItemRepository.saveAll(request));
    }

    @Operation(
        summary = "Get a pharmacy item by ID",
        description = "Retrieves a specific pharmacy item by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Item found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PharmacyItem.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "manufacturer": "PharmaCorp",
                        "name": "Aspirin 100mg",
                        "price": 9.99
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getItem(
        @Parameter(
            description = "ID of the pharmacy item to retrieve",
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) 
        @PathVariable String id
    ) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid UUID format: " + id);
        }
        
        return pharmacyItemRepository.findById(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Update a pharmacy item",
        description = "Updates specific fields of a pharmacy item without requiring the full object. You can update name, manufacturer, and/or price individually or together."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Item updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PharmacyItem.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "manufacturer": "Updated Manufacturer",
                        "name": "Updated Name",
                        "price": 15.99
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid UUID format",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "Invalid UUID format: abc123"
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateItem(
        @Parameter(
            description = "ID of the pharmacy item to update",
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) 
        @PathVariable String id,
        @Parameter(
            description = "Fields to update. You can update any combination of manufacturer, name, and/or price.",
            examples = {
                @ExampleObject(
                    name = "Full update",
                    summary = "Example of updating all fields",
                    value = """
                    {
                        "manufacturer": "Updated Manufacturer Corp",
                        "name": "Updated Medicine Name",
                        "price": 15.99
                    }
                    """
                ),
                @ExampleObject(
                    name = "Partial update - price only",
                    summary = "Example of updating just the price",
                    value = """
                    {
                        "price": 15.99
                    }
                    """
                ),
                @ExampleObject(
                    name = "Partial update - manufacturer and name",
                    summary = "Example of updating manufacturer and name",
                    value = """
                    {
                        "manufacturer": "New Manufacturer Corp",
                        "name": "New Medicine Name"
                    }
                    """
                )
            }
        )
        @RequestBody Map<String, Object> updates
    ) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid UUID format: " + id));
        }

        try {
            PharmacyItem item = pharmacyItemRepository.findById(uuid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

            // Store original values
            String originalManufacturer = item.getManufacturer();
            String originalName = item.getName();
            double originalPrice = item.getPrice();

            // Apply updates
            updates.forEach((key, value) -> {
                try {
                    switch (key) {
                        case "name":
                            if (value == null || ((String) value).trim().isEmpty()) {
                                throw new IllegalArgumentException("Name cannot be empty");
                            }
                            item.setName((String) value);
                            break;
                        case "manufacturer":
                            if (value == null || ((String) value).trim().isEmpty()) {
                                throw new IllegalArgumentException("Manufacturer cannot be empty");
                            }
                            item.setManufacturer((String) value);
                            break;
                        case "price":
                            Double price;
                            if (value instanceof Number) {
                                price = ((Number) value).doubleValue();
                            } else if (value instanceof String) {
                                try {
                                    price = Double.parseDouble((String) value);
                                } catch (NumberFormatException e) {
                                    throw new IllegalArgumentException("Invalid price format");
                                }
                            } else {
                                throw new IllegalArgumentException("Invalid price format");
                            }
                            if (price < 0) {
                                throw new IllegalArgumentException("Price cannot be negative");
                            }
                            item.setPrice(price);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown field: " + key);
                    }
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                }
            });

            // Check for duplicates only if all three unique fields are being updated
            boolean isUpdatingManufacturer = updates.containsKey("manufacturer");
            boolean isUpdatingName = updates.containsKey("name");
            boolean isUpdatingPrice = updates.containsKey("price");

            if (isUpdatingManufacturer && isUpdatingName && isUpdatingPrice) {
                Optional<PharmacyItem> existingItem = pharmacyItemRepository
                    .findByManufacturerAndNameAndPriceAndIdNot(
                        item.getManufacturer(), 
                        item.getName(), 
                        item.getPrice(),
                        item.getId()
                    );
                
                if (existingItem.isPresent()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "An item with the same manufacturer, name, and price already exists",
                        "conflicting_item", existingItem.get()
                    ));
                }
            }

            return ResponseEntity.ok(pharmacyItemRepository.save(item));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }

    @Operation(
        summary = "Search pharmacy items",
        description = "Search items by name, manufacturer, or price range"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = PharmacyItem.class)),
                examples = @ExampleObject(
                    value = """
                    [
                        {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "manufacturer": "PharmaCorp",
                            "name": "Aspirin 100mg",
                            "price": 9.99
                        }
                    ]
                    """
                )
            )
        )
    })
    @GetMapping("/search")
    public ResponseEntity<List<PharmacyItem>> searchItems(
        @Parameter(
            description = "Name of the item (case-insensitive partial match)",
            example = "aspirin"
        ) 
        @RequestParam(required = false) String name,
        @Parameter(
            description = "Manufacturer name (case-insensitive partial match)",
            example = "PharmaCorp"
        ) 
        @RequestParam(required = false) String manufacturer,
        @Parameter(
            description = "Minimum price",
            example = "5.00"
        ) 
        @RequestParam(required = false) Double minPrice,
        @Parameter(
            description = "Maximum price",
            example = "15.00"
        ) 
        @RequestParam(required = false) Double maxPrice,
        @Parameter(
            description = "Exact price match",
            example = "9.99"
        ) 
        @RequestParam(required = false) Double price
    ) {
        List<PharmacyItem> result;
        if (name != null) {
            result = pharmacyItemRepository.findByNameContainingIgnoreCase(name);
        } else if (manufacturer != null) {
            result = pharmacyItemRepository.findByManufacturerContainingIgnoreCase(manufacturer);
        } else if (minPrice != null && maxPrice != null) {
            result = pharmacyItemRepository.findByPriceBetween(minPrice, maxPrice);
        } else if (minPrice != null) {
            result = pharmacyItemRepository.findByPriceGreaterThanOrEqual(minPrice);
        } else if (maxPrice != null) {
            result = pharmacyItemRepository.findByPriceLessThanOrEqual(maxPrice);
        } else if (price != null) {
            result = pharmacyItemRepository.findByPrice(price);
        } else {
            result = pharmacyItemRepository.findAll();
        }
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Delete pharmacy items",
        description = "Delete items based on manufacturer, name, or price range"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Items deleted successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "No search criteria provided",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "error": "At least one search criteria must be provided"
                    }
                    """
                )
            )
        )
    })
    @DeleteMapping
    public ResponseEntity<?> deleteItems(
        @Parameter(
            description = "ID of the pharmacy item to delete",
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) 
        @RequestParam(required = false) String id,
        @Parameter(
            description = "Manufacturer name for deletion",
            example = "PharmaCorp"
        ) 
        @RequestParam(required = false) String manufacturer,
        @Parameter(
            description = "Item name for deletion",
            example = "Aspirin 100mg"
        ) 
        @RequestParam(required = false) String name,
        @Parameter(
            description = "Minimum price for deletion range",
            example = "5.00"
        ) 
        @RequestParam(required = false) Double minPrice,
        @Parameter(
            description = "Maximum price for deletion range",
            example = "15.00"
        ) 
        @RequestParam(required = false) Double maxPrice
    ) {
        long deletedCount = 0;
        
        if (id != null) {
            try {
                UUID uuid = UUID.fromString(id);
                if (pharmacyItemRepository.existsById(uuid)) {
                    pharmacyItemRepository.deleteById(uuid);
                    deletedCount = 1;
                }
                return ResponseEntity.ok()
                    .body(Map.of("message", "Delete operation completed", "deletedCount", deletedCount));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid UUID format: " + id));
            }
        }
        if (manufacturer != null) {
            deletedCount = pharmacyItemRepository.countByManufacturer(manufacturer);
            pharmacyItemRepository.deleteByManufacturer(manufacturer);
            return ResponseEntity.ok()
                .body(Map.of("message", "Delete operation completed", "deletedCount", deletedCount));
        }
        if (name != null) {
            deletedCount = pharmacyItemRepository.countByName(name);
            pharmacyItemRepository.deleteByName(name);
            return ResponseEntity.ok()
                .body(Map.of("message", "Delete operation completed", "deletedCount", deletedCount));
        }
        if (minPrice != null && maxPrice != null) {
            deletedCount = pharmacyItemRepository.countByPriceBetween(minPrice, maxPrice);
            pharmacyItemRepository.deleteByPriceBetween(minPrice, maxPrice);
            return ResponseEntity.ok()
                .body(Map.of("message", "Delete operation completed", "deletedCount", deletedCount));
        }
        if (minPrice != null) {
            deletedCount = pharmacyItemRepository.countByPriceGreaterThanEqual(minPrice);
            pharmacyItemRepository.deleteByPriceGreaterThanEqual(minPrice);
            return ResponseEntity.ok()
                .body(Map.of("message", "Delete operation completed", "deletedCount", deletedCount));
        }
        if (maxPrice != null) {
            deletedCount = pharmacyItemRepository.countByPriceLessThanEqual(maxPrice);
            pharmacyItemRepository.deleteByPriceLessThanEqual(maxPrice);
            return ResponseEntity.ok()
                .body(Map.of("message", "Delete operation completed", "deletedCount", deletedCount));
        }
        return ResponseEntity.badRequest()
            .body(Map.of("error", "At least one search criteria must be provided"));
    }
} 