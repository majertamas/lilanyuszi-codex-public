package com.lilanyuszi.app.testutil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class TestJsonHelper {

    private final ObjectMapper objectMapper;

    public <T> T getData(MvcResult mvcResult, Class<T> clazz) throws Exception {
        JsonNode data = objectMapper.readTree(
                mvcResult.getResponse().getContentAsString()
        ).get("data");

        return objectMapper.treeToValue(data, clazz);
    }
}
