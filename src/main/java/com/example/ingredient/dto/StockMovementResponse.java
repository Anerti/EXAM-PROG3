package com.example.ingredient.dto;

import java.time.Instant;

public class StockMovementResponse {
    private Integer id;
    private Double quantity;
    private String unit;
    private String type;
    private Instant creationDatetime;

    public StockMovementResponse() {
    }

    public StockMovementResponse(Integer id, Double quantity, String unit, String type, Instant creationDatetime) {
        this.id = id;
        this.quantity = quantity;
        this.unit = unit;
        this.type = type;
        this.creationDatetime = creationDatetime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }
}
