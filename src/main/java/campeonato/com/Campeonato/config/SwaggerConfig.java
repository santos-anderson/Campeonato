package campeonato.com.Campeonato.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Campeonato de Futebol",
                version = "1.0",
                description = "API de cadastros de clubes,equipes e partidas, com geração de relatórios como Rankig, Retrospecto etc.",
                contact = @Contact(
                        name = "Anderson Santos",
                        email = "santos.anders@gmail.com",
                        url = "https://github.com/santos-anderson"
                )
        )
)
public class SwaggerConfig {
}
