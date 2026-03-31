package com.example.ingredient.validator;

import com.example.ingredient.entity.Dish;
import com.example.ingredient.exception.BadRequestException;
import com.example.ingredient.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class DishValidator {

    public void validateId(Integer id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid dish id");
        }
    }

    public void validateRequestBody(Object requestBody) {
        if (requestBody == null) {
            throw new BadRequestException("Request body is mandatory");
        }
    }

    public void validateExists(Dish dish, Integer id) {
        if (dish == null) {
            throw new NotFoundException("Dish.id=" + id + " is not found");
        }
    }
}
