package com.lilanyuszi.app.shared_access_alias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import static com.lilanyuszi.app.util.Constant.SHARED_ACCESS_ALIAS_REQUIRED;
import static com.lilanyuszi.app.util.Constant.SHARED_ACCESS_ID_REQUIRED;

public record SharedAccessAliasRequest(
        @NotNull(message = SHARED_ACCESS_ID_REQUIRED)
        Long sharedAccessId,

        @NotBlank(message = SHARED_ACCESS_ALIAS_REQUIRED)
        String alias
) {
}
