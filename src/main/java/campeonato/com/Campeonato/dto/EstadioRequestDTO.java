package campeonato.com.Campeonato.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

@Data
public class EstadioRequestDTO {
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, message = "O nome deve ter pelo menos 3 caracteres")
    private String nome;

    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(
            regexp = "\\d{5}-\\d{3}",
            message = "O CEP deve estar no formato 99999-999"
    )
    private String cep;
}