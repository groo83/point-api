package com.groo83.point.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

   @Bean
   public OpenAPI api() {
       OpenAPI openAPI = new OpenAPI()
               .info(apiInfo());

       return openAPI;
   }


    private Info apiInfo() {
        return new Info()
                .title("Point API Documentation")
                .description("포인트 API")
                .version("1.0.0");
    }

}