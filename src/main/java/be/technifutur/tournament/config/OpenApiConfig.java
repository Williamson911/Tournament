package be.technifutur.tournament.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "TOURNAMENT API",
                version = "1.0",
                description = "Documentation OpenAPI des endpoints TOURNAMENT"
        ),
        servers = @Server(url = "/", description = "Server courant")
)
public class OpenApiConfig {
}

