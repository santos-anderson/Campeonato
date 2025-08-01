package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.config.RabbitMQConfig;
import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import campeonato.com.Campeonato.entity.Clube;
import campeonato.com.Campeonato.entity.Estadio;
import campeonato.com.Campeonato.entity.Partida;
import campeonato.com.Campeonato.repository.ClubeRepository;
import campeonato.com.Campeonato.repository.EstadioRepository;
import campeonato.com.Campeonato.repository.PartidaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartidaConsumer {

    private final PartidaRepository partidaRepository;
    private final ClubeRepository clubeRepository;
    private final EstadioRepository estadioRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PARTIDA)
    public void receberPartida(final PartidaRequestDTO dto) {
        log.info("Recebendo partida: {}", dto);

        try {
            validarDTO(dto);
            validarEProcessarPartida(dto);
            log.info("Partida processada com sucesso!");
        } catch (IllegalArgumentException ex) {
            log.error("Erro: {}. Partida será descartada e enviada à fila de erro.", ex.getMessage());

            throw new AmqpRejectAndDontRequeueException(ex.getMessage());
        } catch (Exception ex) {
            log.error("Erro temporário: {}. Será reprocessado até o limite de tentativas.", ex.getMessage());

            throw ex;
        }
    }

    private void validarEProcessarPartida(PartidaRequestDTO dto) {
        Clube clubeCasa = clubeRepository.findById(dto.getClubeCasaId())
                .orElseThrow(() -> new IllegalArgumentException("Clube da casa não encontrado"));
        Clube clubeVisitante = clubeRepository.findById(dto.getClubeVisitanteId())
                .orElseThrow(() -> new IllegalArgumentException("Clube visitante não encontrado"));
        Estadio estadio = estadioRepository.findById(dto.getEstadioId())
                .orElseThrow(() -> new IllegalArgumentException("Estádio não encontrado"));

        if (!Boolean.TRUE.equals(clubeCasa.getStatus()) || !Boolean.TRUE.equals(clubeVisitante.getStatus())) {
            throw new IllegalArgumentException("Clube inativo.");
        }

        if (dto.getDataHora().toLocalDate().isBefore(clubeCasa.getDataCriacao()) ||
                dto.getDataHora().toLocalDate().isBefore(clubeVisitante.getDataCriacao())) {
            throw new IllegalArgumentException("Data da partida é anterior à criação de um dos clubes.");
        }

        LocalDateTime inicio = dto.getDataHora().minusHours(48);
        LocalDateTime fim = dto.getDataHora().plusHours(48);

        boolean conflito = partidaRepository.existsByClubeCasaIdAndDataHoraBetween(clubeCasa.getId(), inicio, fim)
                || partidaRepository.existsByClubeVisitanteIdAndDataHoraBetween(clubeCasa.getId(), inicio, fim)
                || partidaRepository.existsByClubeCasaIdAndDataHoraBetween(clubeVisitante.getId(), inicio, fim)
                || partidaRepository.existsByClubeVisitanteIdAndDataHoraBetween(clubeVisitante.getId(), inicio, fim);

        if (conflito) {
            throw new IllegalArgumentException("Conflito de horário com outra partida de um dos clubes.");
        }

        LocalDateTime diaIni = dto.getDataHora().toLocalDate().atStartOfDay();
        LocalDateTime diaFim = dto.getDataHora().toLocalDate().atTime(23, 59, 59);

        if (partidaRepository.existsByEstadioIdAndDataHoraBetween(estadio.getId(), diaIni, diaFim)) {
            throw new IllegalArgumentException("Já existe partida nesse estádio nesse dia.");
        }

        Partida partida = new Partida();
        partida.setClubeCasa(clubeCasa);
        partida.setClubeVisitante(clubeVisitante);
        partida.setEstadio(estadio);
        partida.setDataHora(dto.getDataHora());
        partida.setGolsCasa(dto.getGolsCasa());
        partida.setGolsVisitante(dto.getGolsVisitante());

        partidaRepository.save(partida);
    }

    private void validarDTO(PartidaRequestDTO dto) {
        if (dto.getClubeCasaId() == null || dto.getClubeVisitanteId() == null
                || dto.getEstadioId() == null || dto.getDataHora() == null
                || dto.getGolsCasa() == null || dto.getGolsVisitante() == null) {
            throw new IllegalArgumentException("Todos os campos são obrigatórios");
        }

        if (dto.getClubeCasaId().equals(dto.getClubeVisitanteId())) {
            throw new IllegalArgumentException("Clubes não podem ser iguais");
        }

        if (dto.getGolsCasa() < 0 || dto.getGolsVisitante() < 0) {
            throw new IllegalArgumentException("Gols não podem ser negativos");
        }
    }
}
