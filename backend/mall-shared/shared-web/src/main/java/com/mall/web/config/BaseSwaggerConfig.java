package com.mall.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI 配置基类 — 各 BFF 子类可覆盖 title/version/description
 */
public abstract class BaseSwaggerConfig {

    protected abstract String title();
    protected abstract String version();
    protected abstract String description();

    public OpenAPI buildOpenAPI() {
        String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title(title())
                        .version(version())
                        .description(description()))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
