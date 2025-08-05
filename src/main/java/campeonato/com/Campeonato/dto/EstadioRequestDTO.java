package campeonato.com.Campeonato.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EstadioRequestDTO {

    @Schema(
            description = "Nome do estádio",
            example = "Arena Corinthians",
            minLength = 3,
            required = true
    )
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, message = "O nome deve ter pelo menos 3 caracteres")
    private String nome;

    @Schema(
            description = "CEP do estádio, no formato 99999-999",
            example = "12345-678",
            required = true,
            pattern = "\\d{5}-\\d{3}"
    )
    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(
            regexp = "\\d{5}-\\d{3}",
            message = "O CEP deve estar no formato 99999-999"
    )
    private String cep;
}