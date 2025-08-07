package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import campeonato.com.Campeonato.entity.Clube;
import campeonato.com.Campeonato.entity.Estadio;
import campeonato.com.Campeonato.repository.ClubeRepository;
import campeonato.com.Campeonato.repository.EstadioRepository;
import campeonato.com.Campeonato.repository.PartidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PartidaConsumerTest {

    private PartidaRepository partidaRepository;
    private ClubeRepository clubeRepository;
    private EstadioRepository estadioRepository;

    private PartidaConsumer consumer;

    @BeforeEach
    void setUp() {
        partidaRepository = mock(PartidaRepository.class);
        clubeRepository = mock(ClubeRepository.class);
        estadioRepository = mock(EstadioRepository.class);

        consumer = new PartidaConsumer(partidaRepository, clubeRepository, estadioRepository);
    }

    private PartidaRequestDTO criarPartidaValida() {
        PartidaRequestDTO dto = new PartidaRequestDTO();
        dto.setClubeCasaId(1L);
        dto.setClubeVisitanteId(2L);
        dto.setEstadioId(1L);
        dto.setDataHora(LocalDateTime.of(2025, 8, 20, 15, 0));
        dto.setGolsCasa(2);
        dto.setGolsVisitante(1);
        return dto;
    }

    private Clube criarClube(Long id, boolean ativo, LocalDate dataCriacao) {
        Clube clube = new Clube();
        clube.setId(id);
        clube.setStatus(ativo);
        clube.setDataCriacao(dataCriacao);
        return clube;
    }

    private Estadio criarEstadio(Long id) {
        Estadio estadio = new Estadio();
        estadio.setId(id);
        return estadio;
    }

    @Test
    void deveProcessarPartidaComSucesso() {
        PartidaRequestDTO dto = criarPartidaValida();

        Clube clubeCasa = criarClube(1L, true, LocalDate.of(2020, 1, 1));
        Clube clubeVisitante = criarClube(2L, true, LocalDate.of(2020, 1, 1));
        Estadio estadio = criarEstadio(1L);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeVisitante));
        when(estadioRepository.findById(1L)).thenReturn(Optional.of(estadio));

        when(partidaRepository.existsByClubeCasaIdAndDataHoraBetween(anyLong(), any(), any())).thenReturn(false);
        when(partidaRepository.existsByClubeVisitanteIdAndDataHoraBetween(anyLong(), any(), any())).thenReturn(false);
        when(partidaRepository.existsByEstadioIdAndDataHoraBetween(anyLong(), any(), any())).thenReturn(false);

        assertDoesNotThrow(() -> consumer.receberPartida(dto));

        verify(partidaRepository, times(1)).save(any());
    }

    @Test
    void deveRejeitarQuandoDTOInvalido() {
        PartidaRequestDTO dto = new PartidaRequestDTO();

        AmqpRejectAndDontRequeueException exception = assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
            consumer.receberPartida(dto);
        });

        assertTrue(exception.getMessage().contains("Todos os campos são obrigatórios"));
    }

    @Test
    void deveRejeitarQuandoClubesForemIguais() {
        PartidaRequestDTO dto = criarPartidaValida();
        dto.setClubeVisitanteId(1L); // mesmo do clube da casa

        AmqpRejectAndDontRequeueException exception = assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
            consumer.receberPartida(dto);
        });

        assertTrue(exception.getMessage().contains("Clubes não podem ser iguais"));
    }

    @Test
    void deveRejeitarQuandoClubeNaoExiste() {
        PartidaRequestDTO dto = criarPartidaValida();

        when(clubeRepository.findById(1L)).thenReturn(Optional.empty());

        AmqpRejectAndDontRequeueException exception = assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
            consumer.receberPartida(dto);
        });

        assertTrue(exception.getMessage().contains("Clube da casa não encontrado"));
    }

    @Test
    void deveRejeitarQuandoEstadioNaoExiste() {
        PartidaRequestDTO dto = criarPartidaValida();

        Clube clubeCasa = criarClube(1L, true, LocalDate.of(2020, 1, 1));
        Clube clubeVisitante = criarClube(2L, true, LocalDate.of(2020, 1, 1));

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeVisitante));
        when(estadioRepository.findById(1L)).thenReturn(Optional.empty());

        AmqpRejectAndDontRequeueException exception = assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
            consumer.receberPartida(dto);
        });

        assertTrue(exception.getMessage().contains("Estádio não encontrado"));
    }

    @Test
    void deveRejeitarQuandoClubeInativo() {
        PartidaRequestDTO dto = criarPartidaValida();

        Clube clubeCasa = criarClube(1L, false, LocalDate.of(2020, 1, 1)); // inativo
        Clube clubeVisitante = criarClube(2L, true, LocalDate.of(2020, 1, 1));
        Estadio estadio = criarEstadio(1L);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeVisitante));
        when(estadioRepository.findById(1L)).thenReturn(Optional.of(estadio));

        AmqpRejectAndDontRequeueException exception = assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
            consumer.receberPartida(dto);
        });

        assertTrue(exception.getMessage().contains("Clube inativo"));
    }

    @Test
    void deveRejeitarQuandoExisteConflitoDeHorario() {
        PartidaRequestDTO dto = criarPartidaValida();

        Clube clubeCasa = criarClube(1L, true, LocalDate.of(2020, 1, 1));
        Clube clubeVisitante = criarClube(2L, true, LocalDate.of(2020, 1, 1));
        Estadio estadio = criarEstadio(1L);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeVisitante));
        when(estadioRepository.findById(1L)).thenReturn(Optional.of(estadio));

        when(partidaRepository.existsByClubeCasaIdAndDataHoraBetween(anyLong(), any(), any())).thenReturn(true);

        AmqpRejectAndDontRequeueException exception = assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
            consumer.receberPartida(dto);
        });

        assertTrue(exception.getMessage().contains("Conflito de horário"));
    }

    @Test
    void deveRejeitarQuandoEstadioTemPartidaNoMesmoDia() {
        PartidaRequestDTO dto = criarPartidaValida();

        Clube clubeCasa = criarClube(1L, true, LocalDate.of(2020, 1, 1));
        Clube clubeVisitante = criarClube(2L, true, LocalDate.of(2020, 1, 1));
        Estadio estadio = criarEstadio(1L);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeVisitante));
        when(estadioRepository.findById(1L)).thenReturn(Optional.of(estadio));

        when(partidaRepository.existsByClubeCasaIdAndDataHoraBetween(anyLong(), any(), any())).thenReturn(false);
        when(partidaRepository.existsByClubeVisitanteIdAndDataHoraBetween(anyLong(), any(), any())).thenReturn(false);
        when(partidaRepository.existsByEstadioIdAndDataHoraBetween(anyLong(), any(), any())).thenReturn(true);

        AmqpRejectAndDontRequeueException exception = assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
            consumer.receberPartida(dto);
        });

        assertTrue(exception.getMessage().contains("Já existe partida nesse estádio"));
    }

    @Test
    void deveRejeitarQuandoDataPartidaAnteriorCriacaoClube() {
        PartidaRequestDTO dto = criarPartidaValida();

        Clube clubeCasa = criarClube(1L, true, LocalDate.of(2025, 8, 25)); // 5 dias depois da partida
        Clube clubeVisitante = criarClube(2L, true, LocalDate.of(2020, 1, 1));
        Estadio estadio = criarEstadio(1L);

        when(clubeRepository.findById(1L)).thenReturn(Optional.of(clubeCasa));
        when(clubeRepository.findById(2L)).thenReturn(Optional.of(clubeVisitante));
        when(estadioRepository.findById(1L)).thenReturn(Optional.of(estadio));

        AmqpRejectAndDontRequeueException exception = assertThrows(AmqpRejectAndDontRequeueException.class, () -> {
            consumer.receberPartida(dto);
        });

        assertTrue(exception.getMessage().contains("Data da partida é anterior à criação de um dos clubes."));
    }

    @Test
    void deveLancarExcecaoGenericaParaReprocessamento() {
        PartidaRequestDTO dto = criarPartidaValida();

        when(clubeRepository.findById(anyLong()))
                .thenThrow(new RuntimeException("Erro inesperado"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            consumer.receberPartida(dto);
        });

        assertEquals("Erro inesperado", exception.getMessage());
    }


}
