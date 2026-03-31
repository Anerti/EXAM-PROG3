package com.example.ingredient.dto;

import java.util.List;

public class IngredientListRequest {
    private List<IngredientResponse> ingredients;

    public IngredientListRequest() {
    }

    public IngredientListRequest(List<IngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }

    public List<IngredientResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }
}
