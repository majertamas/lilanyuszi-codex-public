package com.lilanyuszi.app.shopping_list;

import jakarta.validation.constraints.NotBlank;

import static com.lilanyuszi.app.util.Constant.SHOPPING_LIST_NAME_REQUIRED;

public record ShoppingListCreateRequest(
        @NotBlank(message = SHOPPING_LIST_NAME_REQUIRED)
        String name
) {
}
