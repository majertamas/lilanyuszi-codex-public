package com.lilanyuszi.app.shared_access;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.api.RestResponse;
import com.lilanyuszi.app.api.RestResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.lilanyuszi.app.util.Constant.API_LIST_PATH;

@RestController
@RequestMapping(API_LIST_PATH)
@RequiredArgsConstructor
public class SharedAccessController {

    private final SharedAccessService sharedAccessService;

    @GetMapping
    public ResponseEntity<RestResponse<List<SharedAccessResponse>>> findAllByUser() throws LilanyusziException {
        List<SharedAccessResponse> allByUser = sharedAccessService.findAllByUser();
        return ResponseEntity.ok(RestResponseUtil.createRestResponse(allByUser));
    }
}
