
package campeonato.com.Campeonato.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClubeRequestDTO {
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 2, message = "O nome deve ter pelo menos 2 caracteres")
    private String nome;

    @NotBlank(message = "A UF é obrigatória")
    @Pattern(
            regexp = "^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$",
            message = "UF inválido"
    )
    private String uf;

    @PastOrPresent(message = "A data de criação não pode ser no futuro")
    @NotNull(message = "A data de criação é obrigatória")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dataCriacao;

    @NotNull(message = "O status é obrigatório")
    private Boolean status;
}