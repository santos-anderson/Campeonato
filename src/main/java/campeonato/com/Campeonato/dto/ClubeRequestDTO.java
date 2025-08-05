package campeonato.com.Campeonato.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClubeRequestDTO {

    @Schema(description = "Nome do clube", example = "Corinthians", minLength = 2, required = true)
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, message = "O nome deve ter pelo menos 2 caracteres")
    private String nome;

    @Schema(
            description = "Sigla da Unidade Federativa (UF) onde o clube está sediado",
            example = "SP",
            required = true,
            allowableValues = {
                    "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE",
                    "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
            }
    )
    @NotBlank(message = "A UF é obrigatória")
    @Pattern(
            regexp = "^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$",
            message = "UF inválido"
    )
    private String uf;

    @Schema(description = "Data de criação do clube (não pode ser no futuro)", example = "1910-09-01", required = true)
    @PastOrPresent(message = "A data de criação não pode ser no futuro")
    @NotNull(message = "A data de criação é obrigatória")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dataCriacao;

    @Schema(description = "Status do clube (ativo = true, inativo = false)", example = "true", required = true)
    @NotNull(message = "O status é obrigatório")
    private Boolean status;
}
