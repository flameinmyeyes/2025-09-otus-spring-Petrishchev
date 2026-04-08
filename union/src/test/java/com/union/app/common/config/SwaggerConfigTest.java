package com.union.app.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {

    @Test
    void buildsOpenApiBean() {
        SwaggerConfig config = new SwaggerConfig();
        var openApi = config.customOpenAPI();

        assertAll(
                () -> assertEquals("Union Messenger API", openApi.getInfo().getTitle()),
                () -> assertEquals("1.0.0", openApi.getInfo().getVersion()),
                () -> assertNotNull(openApi.getComponents().getSecuritySchemes().get("BearerAuth")),
                () -> assertEquals("bearer", openApi.getComponents().getSecuritySchemes().get("BearerAuth").getScheme())
        );
    }
}
