package campeonato.com.Campeonato.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubeRankingDTO {

    @Schema(description = "ID do clube", example = "1")
    private Long clubeId;

    @Schema(description = "Nome do clube", example = "Flamengo")
    private String nome;

    @Schema(description = "Total de pontos acumulados pelo clube", example = "42")
    private int pontos;

    @Schema(description = "Total de gols feitos pelo clube", example = "35")
    private int golsFeitos;

    @Schema(description = "Total de vitórias do clube", example = "13")
    private int vitorias;

    @Schema(description = "Total de jogos disputados pelo clube", example = "18")
    private int jogos;
}