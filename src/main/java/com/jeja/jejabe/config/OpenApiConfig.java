package com.jeja.jejabe.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "청년부 관리 API 명세서",
                description = "교회 청년부 웹페이지를 위한 백엔드 API 명세서입니다.",
                version = "v1.0.0"))
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(){
        // SecuritySecheme명
        String jwtSchemeName = "bearerAuth";
        // API 요청헤더에 인증정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        // SecuritySchemes 등록
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")
                        .bearerFormat("JWT")); // 토큰 타입 명시

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
