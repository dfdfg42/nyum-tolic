package com.nyumtolic.nyumtolic.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    /**
     * OpenAPI bean 구성
     * @return
     */
    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
                .title("냠톨릭 API")
                .version("1.0")
                .description("냠톨릭에서 제공하는 api swagger 문서입니다.");

        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}
