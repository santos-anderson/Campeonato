package campeonato.com.Campeonato.dto;


import lombok.Data;

@Data
public class ClubeRankingDTO {
    private Long clubeId;
    private String nome;
    private int pontos;
    private int golsFeitos;
    private int vitorias;
    private int jogos;

    public ClubeRankingDTO(Long clubeId, String nome, int pontos, int golsFeitos, int vitorias, int jogos) {
        this.clubeId = clubeId;
        this.nome = nome;
        this.pontos = pontos;
        this.golsFeitos = golsFeitos;
        this.vitorias = vitorias;
        this.jogos = jogos;
    }

}