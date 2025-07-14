package campeonato.com.Campeonato.dto;

import lombok.Data;

@Data
public class RetrospectoContraDTO {
    private Long adversarioId;
    private String adversarioNome;
    private int vitorias;
    private int empates;
    private int derrotas;
    private int golsFeitos;
    private int golsSofridos;

    public RetrospectoContraDTO(Long adversarioId, String adversarioNome,
                                int vitorias, int empates, int derrotas, int golsFeitos, int golsSofridos) {
        this.adversarioId = adversarioId;
        this.adversarioNome = adversarioNome;
        this.vitorias = vitorias;
        this.empates = empates;
        this.derrotas = derrotas;
        this.golsFeitos = golsFeitos;
        this.golsSofridos = golsSofridos;
    }

}
