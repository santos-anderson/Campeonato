package campeonato.com.Campeonato.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PartidaRequestDTO  implements Serializable {
    @NotNull(message = "Id do clube da casa é obrigatório")
    private Long clubeCasaId;

    @NotNull(message = "Id do clube visitante é obrigatório")
    private Long clubeVisitanteId;

    @NotNull(message = "Id do estádio é obrigatório")
    private Long estadioId;

    @NotNull(message = "Data e hora é obrigatória")
    @FutureOrPresent(message = "A data e hora da partida não pode ser no passado")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHora;

    @NotNull(message = "Gols do Mandante é obrigatório")
    @Min(value = 0, message = "Gols não podem ser negativos")
    private Integer golsCasa;

    @NotNull(message = "Gols do visitante é obrigatório")
    @Min(value = 0, message = "Gols não podem ser negativos")
    private Integer golsVisitante;
}