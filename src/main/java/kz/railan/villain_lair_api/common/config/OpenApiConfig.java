package kz.railan.villain_lair_api.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI villainLairOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Villain Lair API")
                        .version("1.0")
                        .description("REST backend for a command-based hero vs villain strategy game. Heroes raid villain lairs for coins; villains fortify their defences and earn rewards for successful defence. All endpoints except /auth/** require a Bearer JWT obtained from POST /api/v1/auth/login."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
