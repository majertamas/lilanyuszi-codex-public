package com.lilanyuszi.app.shopping_list;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.api.RestResponse;
import com.lilanyuszi.app.api.RestResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.lilanyuszi.app.util.Constant.API_SHOPPING_LIST_PATH;

@RestController
@RequestMapping(API_SHOPPING_LIST_PATH)
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @PostMapping
    public ResponseEntity<RestResponse<ShoppingListResponse>> create(
            @Valid @RequestBody ShoppingListCreateRequest request
    ) throws LilanyusziException {
        return ResponseEntity.ok(RestResponseUtil.createRestResponse(shoppingListService.create(request)));
    }

    @GetMapping
    public ResponseEntity<RestResponse<List<ShoppingListResponse>>> findAll() throws LilanyusziException {
        return ResponseEntity.ok(RestResponseUtil.createRestResponse(shoppingListService.findAllForAuthenticatedUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ShoppingListResponse>> findById(
            @PathVariable Long id
    ) throws LilanyusziException {
        return ResponseEntity.ok(RestResponseUtil.createRestResponse(shoppingListService.findById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteById(
            @PathVariable Long id
    ) throws LilanyusziException {
        shoppingListService.deleteById(id);
        return ResponseEntity.ok(RestResponseUtil.createVoidRestResponse());
    }

    @DeleteMapping("/{id}/members/me")
    public ResponseEntity<RestResponse<Void>> leave(
            @PathVariable Long id
    ) throws LilanyusziException {
        shoppingListService.leave(id);
        return ResponseEntity.ok(RestResponseUtil.createVoidRestResponse());
    }
}
