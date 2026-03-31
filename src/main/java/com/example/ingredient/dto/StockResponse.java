package com.example.ingredient.dto;

import com.example.ingredient.entity.Unit;

public class StockResponse {
    private Unit unit;
    private Double quantity;

    public StockResponse() {
    }

    public StockResponse(Unit unit, Double quantity) {
        this.unit = unit;
        this.quantity = quantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
}
