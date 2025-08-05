package campeonato.com.Campeonato.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PartidaRequestDTO implements Serializable {

    @Schema(
            description = "ID do clube mandante (casa)",
            example = "1",
            required = true
    )
    @NotNull(message = "Id do clube da casa é obrigatório")
    private Long clubeCasaId;

    @Schema(
            description = "ID do clube visitante",
            example = "2",
            required = true
    )
    @NotNull(message = "Id do clube visitante é obrigatório")
    private Long clubeVisitanteId;

    @Schema(
            description = "ID do estádio onde ocorrerá a partida",
            example = "5",
            required = true
    )
    @NotNull(message = "Id do estádio é obrigatório")
    private Long estadioId;

    @Schema(
            description = "Data e hora da partida (não pode ser no passado)",
            example = "2025-08-10T16:00:00",
            type = "string",
            format = "date-time",
            required = true
    )
    @NotNull(message = "Data e hora é obrigatória")
    @FutureOrPresent(message = "A data e hora da partida não pode ser no passado")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataHora;

    @Schema(
            description = "Quantidade de gols marcados pelo clube da casa (mandante)",
            example = "2",
            minimum = "0",
            required = true
    )
    @NotNull(message = "Gols do Mandante é obrigatório")
    @Min(value = 0, message = "Gols não podem ser negativos")
    private Integer golsCasa;

    @Schema(
            description = "Quantidade de gols marcados pelo clube visitante",
            example = "1",
            minimum = "0",
            required = true
    )
    @NotNull(message = "Gols do visitante é obrigatório")
    @Min(value = 0, message = "Gols não podem ser negativos")
    private Integer golsVisitante;
}
