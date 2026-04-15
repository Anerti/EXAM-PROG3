package com.example.ingredient.controller;

import com.example.ingredient.dto.IngredientResponse;
import com.example.ingredient.dto.StockMovementRequest;
import com.example.ingredient.dto.StockMovementResponse;
import com.example.ingredient.dto.StockResponse;
import com.example.ingredient.entity.*;
import com.example.ingredient.repository.IngredientRepository;
import com.example.ingredient.validator.IngredientValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class IngredientController {

    private final IngredientRepository ingredientRepository;
    private final IngredientValidator validator;

    public IngredientController(IngredientRepository ingredientRepository, IngredientValidator validator) {
        this.ingredientRepository = ingredientRepository;
        this.validator = validator;
    }

    @GetMapping("/ingredients")
    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/ingredients/{id}")
    public IngredientResponse getIngredientById(@PathVariable Integer id) {
        validator.validateId(id);
        
        Ingredient ingredient = ingredientRepository.findById(id);
        validator.validateExists(ingredient, id);
        return toResponse(ingredient);
    }

    @GetMapping("/ingredients/{id}/stock")
    public StockResponse getIngredientStock(
            @PathVariable Integer id,
            @RequestParam("at") String at,
            @RequestParam("unit") String unit) {
        
        validator.validateId(id);
        validator.validateStockParams(at, unit);

        Ingredient ingredient = ingredientRepository.findById(id);
        validator.validateExists(ingredient, id);

        Instant time = Instant.parse(at);
        Unit unitEnum = Unit.valueOf(unit);
        
        var stockValue = ingredientRepository.getStockValueAt(time, id, unitEnum);
        validator.validateExists(stockValue != null ? ingredient : null, id);
        
        return new StockResponse(stockValue.getUnit(), stockValue.getQuantity());
    }

    @GetMapping("/ingredients/{id}/stockMovements")
    public List<StockMovementResponse> getStockMovements(
            @PathVariable Integer id,
            @RequestParam("from") String from,
            @RequestParam("to") String to) {
        
        validator.validateId(id);
        validator.validateStockMovementParams(from, to);

        Ingredient ingredient = ingredientRepository.findById(id);
        validator.validateExists(ingredient, id);

        Instant fromInstant = Instant.parse(from);
        Instant toInstant = Instant.parse(to);

        return ingredientRepository.findStockMovementsByIngredientIdAndDateRange(id, fromInstant, toInstant)
                .stream()
                .map(this::toStockMovementResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/ingredients/{id}/stockMovements")
    public ResponseEntity<StockMovementResponse> createStockMovement(
            @PathVariable Integer id,
            @RequestBody StockMovementRequest request) {
        
        validator.validateId(id);
        validator.validateStockMovementRequest(request);

        Ingredient ingredient = ingredientRepository.findById(id);
        validator.validateExists(ingredient, id);

        StockMovement stockMovement = new StockMovement();
        stockMovement.setType(MovementTypeEnum.valueOf(request.getType()));
        stockMovement.setCreationDatetime(request.getCreationDatetime() != null ? request.getCreationDatetime() : Instant.now());

        var stockValue = new StockValue();
        stockValue.setQuantity(request.getQuantity());
        stockValue.setUnit(Unit.valueOf(request.getUnit()));
        stockMovement.setValue(stockValue);

        ingredientRepository.saveStockMovement(stockMovement, id);

        return ResponseEntity.ok(toStockMovementResponse(stockMovement));
    }

    private IngredientResponse toResponse(Ingredient ingredient) {
        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getCategory(),
                ingredient.getPrice()
        );
    }

    private StockMovementResponse toStockMovementResponse(StockMovement stockMovement) {
        return new StockMovementResponse(
                stockMovement.getId(),
                stockMovement.getValue().getQuantity(),
                stockMovement.getValue().getUnit().name(),
                stockMovement.getType().name(),
                stockMovement.getCreationDatetime()
        );
    }
}
