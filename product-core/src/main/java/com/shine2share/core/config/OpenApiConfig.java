package com.shine2share.core.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI getOpenApiDocumentation() {
        return new OpenAPI()
                .info(new Info().title("product-cloud")
                        .description("demo product cloud")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("shine2share")
                                .url("github.com/shine2share")
                                .email("shine2share@gmail.com")
                        )
                        .termsOfService("this is the terms of service")
                        .license(new License()
                                .name("license's name")
                                .url("license's url")
                        ))
                .externalDocs(new ExternalDocumentation()
                        .description("description external docs")
                        .url("external doc's url"));

    }
}
