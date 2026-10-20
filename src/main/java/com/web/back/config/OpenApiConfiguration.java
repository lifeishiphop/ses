package com.web.back.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI sesOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("S.E.S API").version("v1")
                        .description("현재 컨트롤러와 DTO를 기준으로 생성한 API 문서입니다. "
                                + "/login 응답의 token을 Authorize에 입력하면 인증 API를 호출할 수 있습니다. "
                                + "검색의 page는 1부터 시작합니다. "
                                + "Java·C++ 채점에는 Docker와 ses-judge 이미지가 필요합니다. README의 채점기 실행 방법을 확인하세요."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSchemas("ApiError", new ObjectSchema()
                                .addProperty("error - ", new StringSchema().example("요청을 처리할 수 없습니다."))))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
