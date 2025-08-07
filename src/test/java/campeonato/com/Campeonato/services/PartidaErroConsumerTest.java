package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PartidaErroConsumerTest {

    private final PartidaErroConsumer consumer = new PartidaErroConsumer();

    @Test
    void deveProcessarMensagemDeErroSemExcecao() {
        PartidaRequestDTO dto = criarDtoExemplo();
        assertDoesNotThrow(() -> consumer.receberErro(dto));
    }

    @Test
    void deveLogarMensagemDeErro() {
        PartidaRequestDTO dto = criarDtoExemplo();

        LogCaptor logCaptor = LogCaptor.forClass(PartidaErroConsumer.class);

        consumer.receberErro(dto);

        assertTrue(logCaptor.getErrorLogs().stream()
                .anyMatch(log -> log.contains("Partida não processada após tentativas")));
    }

    private PartidaRequestDTO criarDtoExemplo() {
        PartidaRequestDTO dto = new PartidaRequestDTO();
        dto.setClubeCasaId(1L);
        dto.setClubeVisitanteId(2L);
        dto.setEstadioId(3L);
        dto.setDataHora(java.time.LocalDateTime.now());
        dto.setGolsCasa(1);
        dto.setGolsVisitante(2);
        return dto;
    }
}
