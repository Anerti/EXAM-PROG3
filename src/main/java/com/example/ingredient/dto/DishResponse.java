package com.example.ingredient.dto;

import java.util.List;

public class DishResponse {
    private Integer id;
    private String name;
    private Double price;
    private List<IngredientResponse> ingredients;

    public DishResponse() {
    }

    public DishResponse(Integer id, String name, Double price, List<IngredientResponse> ingredients) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.ingredients = ingredients;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<IngredientResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }
}
