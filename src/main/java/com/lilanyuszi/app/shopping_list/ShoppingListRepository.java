package com.lilanyuszi.app.shopping_list;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    List<ShoppingList> findBySharedAccessIdIn(Collection<Long> sharedAccessIds);
}
