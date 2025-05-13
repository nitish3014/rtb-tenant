package com.rtb.tenant.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public OpenAPI defineOpenApi() {
        Info information = new Info()
                .title(this.appName)
                .version("1.0")
                .description("This API exposes endpoints for Candidate Service");
        OpenAPI openAPI =  new OpenAPI();
        openAPI.info(information);
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList("Bearer Authentication");
        openAPI.addSecurityItem(securityRequirement);
        Components components = new Components();
        components.addSecuritySchemes("Bearer Authentication", createAPIKeyScheme());
        openAPI.components(components);
        return openAPI;
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
