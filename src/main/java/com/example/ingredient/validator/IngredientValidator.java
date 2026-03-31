package com.example.ingredient.validator;

import com.example.ingredient.dto.StockMovementRequest;
import com.example.ingredient.entity.Ingredient;
import com.example.ingredient.exception.BadRequestException;
import com.example.ingredient.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class IngredientValidator {

    public void validateId(Integer id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid ingredient id");
        }
    }

    public void validateStockParams(String at, String unit) {
        if (at == null || at.isEmpty()) {
            throw new BadRequestException("Either mandatory query parameter `at` or `unit` is not provided.");
        }
        if (unit == null || unit.isEmpty()) {
            throw new BadRequestException("Either mandatory query parameter `at` or `unit` is not provided.");
        }
    }

    public void validateStockMovementParams(String from, String to) {
        if (from == null || from.isEmpty()) {
            throw new BadRequestException("Either mandatory query parameter `from` or `to` is not provided.");
        }
        if (to == null || to.isEmpty()) {
            throw new BadRequestException("Either mandatory query parameter `from` or `to` is not provided.");
        }
    }

    public void validateStockMovementRequest(StockMovementRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is mandatory");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity is mandatory and must be positive");
        }
        if (request.getUnit() == null || request.getUnit().isEmpty()) {
            throw new BadRequestException("Unit is mandatory");
        }
        if (request.getType() == null || request.getType().isEmpty()) {
            throw new BadRequestException("Type is mandatory");
        }
    }

    public void validateExists(Ingredient ingredient, Integer id) {
        if (ingredient == null) {
            throw new NotFoundException("Ingredient.id=" + id + " is not found");
        }
    }
}
