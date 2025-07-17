package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import campeonato.com.Campeonato.exception.PartidaCadastroException;
import campeonato.com.Campeonato.exception.PartidaValidacaoException;
import campeonato.com.Campeonato.model.Clube;
import campeonato.com.Campeonato.model.Estadio;
import campeonato.com.Campeonato.model.Partida;
import campeonato.com.Campeonato.repository.ClubeRepository;
import campeonato.com.Campeonato.repository.EstadioRepository;
import campeonato.com.Campeonato.repository.PartidaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PartidaServiceTest {

    @Autowired
    private PartidaService partidaService;

    @Autowired
    private ClubeRepository clubeRepository;
    @Autowired
    private EstadioRepository estadioRepository;
    @Autowired
    private PartidaRepository partidaRepository;


    private Clube criarSalvarClube(String nome, String uf, LocalDate dataCriacao, boolean status) {
        Clube clube = new Clube();
        clube.setNome(nome);
        clube.setUf(uf);
        clube.setDataCriacao(dataCriacao);
        clube.setStatus(status);
        return clubeRepository.save(clube);
    }

    private Estadio criarSalvarEstadio(String nome) {
        Estadio estadio = new Estadio();
        estadio.setNome(nome);
        return estadioRepository.save(estadio);
    }

    private PartidaRequestDTO criarPartidaDTO(Long clubeCasaId, Long clubeVisitanteId, Long estadioId,
                                              LocalDateTime dataHora, Integer golsCasa, Integer golsVisitante) {
        PartidaRequestDTO dto = new PartidaRequestDTO();
        dto.setClubeCasaId(clubeCasaId);
        dto.setClubeVisitanteId(clubeVisitanteId);
        dto.setEstadioId(estadioId);
        dto.setDataHora(dataHora);
        dto.setGolsCasa(golsCasa);
        dto.setGolsVisitante(golsVisitante);
        return dto;
    }

    private void criarSalvarPartida(Clube casa, Clube visitante, Estadio estadio,
                                    LocalDateTime dataHora, int golsCasa, int golsVisitante) {
        Partida partida = new Partida();
        partida.setClubeCasa(casa);
        partida.setClubeVisitante(visitante);
        partida.setEstadio(estadio);
        partida.setDataHora(dataHora);
        partida.setGolsCasa(golsCasa);
        partida.setGolsVisitante(golsVisitante);
        partidaRepository.save(partida);
    }

    @Test
    void cadastrarPartidaComSucesso() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        PartidaRequestDTO dto = criarPartidaDTO(
                casa.getId(), visitante.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 20, 21, 0),
                2, 1);

        String msg = partidaService.cadastrarPartida(dto);

        assertTrue(msg.contains("Partida cadastrada"));
        assertEquals(1, partidaRepository.count());
    }

    @Test
    void cadastrarPartidaCamposObrigatorios() {
        PartidaRequestDTO dto = new PartidaRequestDTO();
        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(dto)
        );
        assertEquals("Todos os campos são obrigatórios.", ex.getMessage());
    }

    @Test
    void cadastrarPartidaClubesIguais() {
        Clube clube = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        PartidaRequestDTO dto = criarPartidaDTO(
                clube.getId(), clube.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 21, 21, 0), 2, 1);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(dto)
        );
        assertEquals("Clubes não podem ser iguais!", ex.getMessage());
    }

    @Test
    void cadastrarPartidaGolsNegativos() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        PartidaRequestDTO dto = criarPartidaDTO(
                casa.getId(), visitante.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 21, 20, 0), -1, 2);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(dto)
        );
        assertEquals("Gols não podem ser negativos!", ex.getMessage());
    }

    @Test
    void cadastrarPartidaClubeCasaNaoExiste() {
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        PartidaRequestDTO dto = criarPartidaDTO(
                99L, visitante.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 21, 21, 0), 1, 1);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(dto)
        );
        assertEquals("Clube da casa não existe!", ex.getMessage());
    }

    @Test
    void cadastrarPartidaClubeVisitanteNaoExiste() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        PartidaRequestDTO dto = criarPartidaDTO(
                casa.getId(), 99L, estadio.getId(),
                LocalDateTime.of(2025, 7, 21, 21, 0), 1, 1);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(dto)
        );
        assertEquals("Clube visitante não existe!", ex.getMessage());
    }

    @Test
    void cadastrarPartidaEstadioNaoExiste() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        PartidaRequestDTO dto = criarPartidaDTO(
                casa.getId(), visitante.getId(), 999L,
                LocalDateTime.of(2025, 7, 21, 22, 0), 1, 1);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(dto)
        );
        assertEquals("Estádio não existe!", ex.getMessage());
    }

    @Test
    void cadastrarPartidaClubeCasaInativo() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), false);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        PartidaRequestDTO dto = criarPartidaDTO(
                casa.getId(), visitante.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 21, 23, 0), 1, 1);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.cadastrarPartida(dto)
        );
        assertEquals("Clube da casa está inativo.", ex.getMessage());
    }

    @Test
    void cadastrarPartidaClubeVisitanteInativo() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), false);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        PartidaRequestDTO dto = criarPartidaDTO(
                casa.getId(), visitante.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 22, 10, 0), 1, 1);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.cadastrarPartida(dto)
        );
        assertEquals("Clube visitante está inativo.", ex.getMessage());
    }

    @Test
    void cadastrarPartidaDataAntesDeCriacaoDoClube() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(2024, 8, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(2014, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        PartidaRequestDTO dto = criarPartidaDTO(
                casa.getId(), visitante.getId(), estadio.getId(),
                LocalDateTime.of(2024, 7, 20, 21, 0), 1, 1);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.cadastrarPartida(dto)
        );
        assertEquals("Data da partida não pode ser anterior à data de criação de algum clube.", ex.getMessage());
    }

    @Test
    void cadastrarPartidaMenosDe48hIntervalo() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(casa, visitante, estadio, LocalDateTime.of(2025, 7, 23, 21, 0), 1, 1);

        PartidaRequestDTO dto = criarPartidaDTO(
                casa.getId(), visitante.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 24, 20, 0), 1, 1);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.cadastrarPartida(dto)
        );
        assertEquals("Um dos clubes já tem partida marcada em menos de 48h.", ex.getMessage());
    }

    @Test
    void cadastrarPartidaEstadioOcupadoMesmoDia() {
        Clube casa1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante1 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(casa1, visitante1, estadio, LocalDateTime.of(2025, 7, 25, 19, 0), 1, 1);

        Clube casa2 = criarSalvarClube("Flamengo", "RJ", LocalDate.of(1931, 1, 1), true);
        Clube visitante2 = criarSalvarClube("Gremio", "RS", LocalDate.of(1903, 9, 15), true);

        PartidaRequestDTO dto = criarPartidaDTO(
                casa2.getId(), visitante2.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 25, 21, 0), 2, 2);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.cadastrarPartida(dto)
        );
        assertEquals("Já existe partida nesse estádio nesse dia!", ex.getMessage());
    }
}
