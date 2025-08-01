package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.config.RabbitMQConfig;
import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PartidaErroConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PARTIDA_ERRO)
    public void receberErro(PartidaRequestDTO dto) {
        log.error("[FILA ERRO] Partida não processada após tentativas: {}", dto);
    }
}
