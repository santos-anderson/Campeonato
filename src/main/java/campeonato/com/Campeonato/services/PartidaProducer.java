package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.config.RabbitMQConfig;
import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PartidaProducer {

    private final RabbitTemplate rabbitTemplate;

    public PartidaProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarPartidaParaFila(PartidaRequestDTO dto) {
        try {
            log.info("Enviando partida para fila: {}", dto);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_PARTIDA,
                    RabbitMQConfig.ROUTING_KEY_PARTIDA,
                    dto
            );
        } catch (Exception e) {
            log.error("Erro ao enviar partida para fila", e);
        }
    }
}
