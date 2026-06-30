package com.lilanyuszi.app.shared_access_alias;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.api.RestResponse;
import com.lilanyuszi.app.api.RestResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.lilanyuszi.app.util.Constant.API_SHARED_ACCESS_ALIAS_PATH;

@RestController
@RequestMapping(API_SHARED_ACCESS_ALIAS_PATH)
@RequiredArgsConstructor
public class SharedAccessAliasController {

    private final SharedAccessAliasService sharedAccessAliasService;

    @PostMapping
    public ResponseEntity<RestResponse<Void>> create(
            @Valid @RequestBody SharedAccessAliasRequest request
    ) throws LilanyusziException {
        sharedAccessAliasService.create(request);
        return ResponseEntity.ok(RestResponseUtil.createVoidRestResponse());
    }

}
