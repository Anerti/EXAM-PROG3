package com.example.ingredient.controller;

import com.example.ingredient.dto.DishResponse;
import com.example.ingredient.dto.IngredientListRequest;
import com.example.ingredient.dto.IngredientResponse;
import com.example.ingredient.entity.Dish;
import com.example.ingredient.entity.DishIngredient;
import com.example.ingredient.entity.Ingredient;
import com.example.ingredient.repository.DishRepository;
import com.example.ingredient.repository.IngredientRepository;
import com.example.ingredient.validator.DishValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DishController {

    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final DishValidator validator;

    public DishController(DishRepository dishRepository, IngredientRepository ingredientRepository, DishValidator validator) {
        this.dishRepository = dishRepository;
        this.ingredientRepository = ingredientRepository;
        this.validator = validator;
    }

    @GetMapping("/dishes")
    public List<DishResponse> getAllDishes() {
        return dishRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/dishes/{id}/ingredients")
    public List<IngredientResponse> getDishIngredients(@PathVariable Integer id) {
        validator.validateId(id);
        
        Dish dish = dishRepository.findById(id);
        validator.validateExists(dish, id);

        return dish.getDishIngredients().stream()
                .map(this::toIngredientResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/dishes/{id}/ingredients")
    public ResponseEntity<Void> updateDishIngredients(
            @PathVariable Integer id,
            @RequestBody IngredientListRequest request) {
        
        validator.validateId(id);
        validator.validateRequestBody(request);

        Dish dish = dishRepository.findById(id);
        validator.validateExists(dish, id);

        List<Ingredient> validIngredients = request.getIngredients().stream()
                .map(ing -> ingredientRepository.findById(ing.getId()))
                .filter(ing -> ing != null)
                .collect(Collectors.toList());

        dishRepository.updateDishIngredients(id, validIngredients);

        return ResponseEntity.ok().build();
    }

    private DishResponse toResponse(Dish dish) {
        List<IngredientResponse> ingredients = dish.getDishIngredients().stream()
                .map(this::toIngredientResponse)
                .collect(Collectors.toList());
        
        return new DishResponse(
                dish.getId(),
                dish.getName(),
                dish.getPrice(),
                ingredients
        );
    }

    private IngredientResponse toIngredientResponse(DishIngredient dishIngredient) {
        Ingredient ingredient = dishIngredient.getIngredient();
        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getCategory(),
                ingredient.getPrice()
        );
    }
}
