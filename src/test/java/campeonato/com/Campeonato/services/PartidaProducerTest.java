package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.config.RabbitMQConfig;
import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class PartidaProducerTest {

    private RabbitTemplate rabbitTemplate;
    private PartidaProducer producer;
    private LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        producer = new PartidaProducer(rabbitTemplate);
        logCaptor = LogCaptor.forClass(PartidaProducer.class);
        logCaptor.clearLogs();
    }

    private PartidaRequestDTO criarPartidaDTO() {
        PartidaRequestDTO dto = new PartidaRequestDTO();
        dto.setClubeCasaId(1L);
        dto.setClubeVisitanteId(2L);
        dto.setEstadioId(3L);
        dto.setDataHora(LocalDateTime.of(2025, 8, 20, 15, 0));
        dto.setGolsCasa(2);
        dto.setGolsVisitante(1);
        return dto;
    }

    @Test
    void deveEnviarMensagemParaFilaComSucesso() {
        PartidaRequestDTO dto = criarPartidaDTO();

        producer.enviarPartidaParaFila(dto);

        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitMQConfig.EXCHANGE_PARTIDA,
                RabbitMQConfig.ROUTING_KEY_PARTIDA,
                dto
        );

        assertTrue(logCaptor.getInfoLogs().stream()
                .anyMatch(log -> log.contains("Enviando partida para fila")), "Deveria conter log de envio");
    }

    @Test
    void deveLogarErroAoEnviarMensagem() {
        PartidaRequestDTO dto = criarPartidaDTO();

        doThrow(new RuntimeException("Erro de envio")).when(rabbitTemplate)
                .convertAndSend(
                        eq(RabbitMQConfig.EXCHANGE_PARTIDA),
                        eq(RabbitMQConfig.ROUTING_KEY_PARTIDA),
                        any(PartidaRequestDTO.class)
                );

        producer.enviarPartidaParaFila(dto);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_PARTIDA),
                eq(RabbitMQConfig.ROUTING_KEY_PARTIDA),
                eq(dto)
        );

        assertTrue(logCaptor.getErrorLogs().stream()
                .anyMatch(log -> log.contains("Erro ao enviar partida para fila")), "Deveria conter log de erro");
    }

}
