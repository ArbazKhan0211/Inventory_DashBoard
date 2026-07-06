package com.example.inventory.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Represents a single stock movement record.
 *
 * Matches the shape of the sample dataset:
 * {
 *   "id": "mv1001",
 *   "timestamp": "2026-03-10T17:46:00Z",
 *   "sku": "SKU003",
 *   "movementType": "IN",
 *   "quantity": 48,
 *   "warehouse": "WH-NORTH"   // optional, bonus field
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Movement {

    private String id;
    private Instant timestamp;
    private String sku;
    private String movementType; // IN | OUT
    private int quantity;
    private String warehouse; // optional / bonus

    public Movement() {
    }

    public Movement(String id, Instant timestamp, String sku, String movementType, int quantity, String warehouse) {
        this.id = id;
        this.timestamp = timestamp;
        this.sku = sku;
        this.movementType = movementType;
        this.quantity = quantity;
        this.warehouse = warehouse;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }
}
