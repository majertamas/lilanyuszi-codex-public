package com.lilanyuszi.app.shopping_item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;

    public ShoppingItem save(ShoppingItem shoppingItem) {
        ShoppingItem savedShoppingItem = shoppingItemRepository.save(shoppingItem);
        log.info("SAVED SHOPPING ITEM: {}", savedShoppingItem);
        return savedShoppingItem;
    }

    public Optional<ShoppingItem> findById(Long id) {
        return shoppingItemRepository.findById(id);
    }

    public List<ShoppingItem> findAll() {
        return shoppingItemRepository.findAll();
    }

    public void deleteById(Long id) {
        shoppingItemRepository.deleteById(id);
        log.info("DELETED SHOPPING ITEM ID: {}", id);
    }
}
