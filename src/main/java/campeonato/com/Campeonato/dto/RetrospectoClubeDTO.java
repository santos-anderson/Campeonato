package campeonato.com.Campeonato.dto;


import lombok.Data;

@Data
public class RetrospectoClubeDTO {
    private int vitorias;
    private int empates;
    private int derrotas;
    private int golsFeitos;
    private int golsSofridos;

    public RetrospectoClubeDTO(int vitorias, int empates, int derrotas, int golsFeitos, int golsSofridos) {
        this.vitorias = vitorias;
        this.empates = empates;
        this.derrotas = derrotas;
        this.golsFeitos = golsFeitos;
        this.golsSofridos = golsSofridos;
    }

}