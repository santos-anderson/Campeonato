package campeonato.com.Campeonato.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetrospectoClubeDTO {

    @Schema(description = "Quantidade de vitórias do clube", example = "10")
    private int vitorias;

    @Schema(description = "Quantidade de empates do clube", example = "5")
    private int empates;

    @Schema(description = "Quantidade de derrotas do clube", example = "3")
    private int derrotas;

    @Schema(description = "Total de gols feitos pelo clube", example = "28")
    private int golsFeitos;

    @Schema(description = "Total de gols sofridos pelo clube", example = "15")
    private int golsSofridos;
}