package com.example.ingredient.entity;

import java.util.ArrayList;
import java.util.List;

public class Dish {
    private Integer id;
    private Double price;
    private String name;
    private DishTypeEnum dishType;
    private List<DishIngredient> dishIngredients;

    public Dish() {
    }

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        if (dishIngredients == null) {
            this.dishIngredients = new ArrayList<>();
            return;
        }
        for (DishIngredient ingredient : dishIngredients) {
            ingredient.setDish(this);
        }
        this.dishIngredients = dishIngredients;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }
}
