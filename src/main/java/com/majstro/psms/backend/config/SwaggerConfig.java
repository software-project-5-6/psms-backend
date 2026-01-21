package com.majstro.psms.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        String cognitoDomain = "https://eu-north-10qfcbesah.auth.eu-north-1.amazoncognito.com";

        SecurityScheme oauthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("AWS Cognito OAuth2 PKCE Flow")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl(cognitoDomain + "/oauth2/authorize")
                                .tokenUrl(cognitoDomain + "/oauth2/token")
                                .scopes(new Scopes()
                                        .addString("openid", "OpenID scope")
                                        .addString("email", "Access to email")
                                        .addString("profile", "Access to user profile")

                                )
                        )
                );

        return new OpenAPI()
                .info(new Info()
                        .title("Project Space Management System API")
                        .description("Swagger integrated with AWS Cognito PKCE authorization")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("oauth2"))
                .components(new Components()
                        .addSecuritySchemes("oauth2", oauthScheme));
    }
}