package campeonato.com.Campeonato.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrospectoContraDTO {

    @Schema(description = "ID do adversário", example = "5")
    private Long adversarioId;

    @Schema(description = "Nome do adversário", example = "Clube XYZ")
    private String adversarioNome;

    @Schema(description = "Total de vitórias contra o adversário", example = "3")
    private int vitorias;

    @Schema(description = "Total de empates contra o adversário", example = "1")
    private int empates;

    @Schema(description = "Total de derrotas contra o adversário", example = "2")
    private int derrotas;

    @Schema(description = "Total de gols feitos contra o adversário", example = "10")
    private int golsFeitos;

    @Schema(description = "Total de gols sofridos contra o adversário", example = "8")
    private int golsSofridos;
}
