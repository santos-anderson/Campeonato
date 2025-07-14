
package campeonato.com.Campeonato.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PartidaRequestDTO {

    private Long clubeCasaId;
    private Long clubeVisitanteId;
    private Long estadioId;
    private LocalDateTime dataHora;
    private Integer golsCasa;
    private Integer golsVisitante;

}