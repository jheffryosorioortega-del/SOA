package pe.utpvet.citas.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Servicio de Gestión de Citas")
                        .version("1.0.0")
                        .description("Programa, confirma, reprograma y cancela citas veterinarias. Evita que un veterinario tenga dos citas a la misma hora. Incluye propietarios, mascotas y veterinarios. Requiere token JWT (botón Authorize)."))
                // URL relativa: las pruebas desde Swagger salen por el mismo host desde el que se abrió la pantalla (el API Gateway)
                .servers(List.of(new Server().url("/").description("API Gateway")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
