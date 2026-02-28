package com.mall.admin.config;

import com.mall.web.config.BaseSwaggerConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig extends BaseSwaggerConfig {

    @Override
    protected String title() {
        return "Mall V3 Admin API";
    }

    @Override
    protected String version() {
        return "3.0.0";
    }

    @Override
    protected String description() {
        return "Mall V3 back-office admin API documentation";
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return buildOpenAPI();
    }
}
