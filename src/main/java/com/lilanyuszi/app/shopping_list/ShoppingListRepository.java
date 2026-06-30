package com.lilanyuszi.app.shopping_list;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    List<ShoppingList> findBySharedAccessIdIn(Collection<Long> sharedAccessIds);
    Optional<ShoppingList> findBySharedAccessId(Long sharedAccessId);
}
