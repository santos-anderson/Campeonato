package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import campeonato.com.Campeonato.exception.PartidaCadastroException;
import campeonato.com.Campeonato.exception.PartidaValidacaoException;
import campeonato.com.Campeonato.entity.Clube;
import campeonato.com.Campeonato.entity.Estadio;
import campeonato.com.Campeonato.entity.Partida;
import campeonato.com.Campeonato.repository.ClubeRepository;
import campeonato.com.Campeonato.repository.EstadioRepository;
import campeonato.com.Campeonato.repository.PartidaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
        PartidaRequestDTO partidaRequestDTO = new PartidaRequestDTO();
        partidaRequestDTO.setClubeCasaId(clubeCasaId);
        partidaRequestDTO.setClubeVisitanteId(clubeVisitanteId);
        partidaRequestDTO.setEstadioId(estadioId);
        partidaRequestDTO.setDataHora(dataHora);
        partidaRequestDTO.setGolsCasa(golsCasa);
        partidaRequestDTO.setGolsVisitante(golsVisitante);
        return partidaRequestDTO;
    }

    private Partida criarSalvarPartida(Clube casa, Clube visitante, Estadio estadio,
                                    LocalDateTime dataHora, int golsCasa, int golsVisitante) {
        Partida partida = new Partida();
        partida.setClubeCasa(casa);
        partida.setClubeVisitante(visitante);
        partida.setEstadio(estadio);
        partida.setDataHora(dataHora);
        partida.setGolsCasa(golsCasa);
        partida.setGolsVisitante(golsVisitante);
        return partidaRepository.save(partida);
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
        PartidaRequestDTO partidaRequestDTO = new PartidaRequestDTO();
        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(partidaRequestDTO)
        );
        assertEquals("Todos os campos são obrigatórios.", ex.getMessage());
    }

    @Test
    void cadastrarPartidaClubesIguais() {
        Clube clube = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        PartidaRequestDTO  partidaRequestDTO= criarPartidaDTO(
                clube.getId(), clube.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 21, 21, 0), 2, 1);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(partidaRequestDTO)
        );
        assertEquals("Clubes não podem ser iguais!", ex.getMessage());
    }

    @Test
    void cadastrarPartidaGolsNegativos() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                casa.getId(), visitante.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 21, 20, 0), -1, 2);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(partidaRequestDTO)
        );
        assertEquals("Gols não podem ser negativos!", ex.getMessage());
    }

    @Test
    void cadastrarPartidaClubeCasaNaoExiste() {
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                99L, visitante.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 21, 21, 0), 1, 1);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(partidaRequestDTO)
        );
        assertEquals("Clube da casa não existe!", ex.getMessage());
    }

    @Test
    void cadastrarPartidaClubeVisitanteNaoExiste() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        PartidaRequestDTO  partidaRequestDTO= criarPartidaDTO(
                casa.getId(), 99L, estadio.getId(),
                LocalDateTime.of(2025, 7, 21, 21, 0), 1, 1);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(partidaRequestDTO)
        );
        assertEquals("Clube visitante não existe!", ex.getMessage());
    }

    @Test
    void cadastrarPartidaEstadioNaoExiste() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                casa.getId(), visitante.getId(), 999L,
                LocalDateTime.of(2025, 7, 21, 22, 0), 1, 1);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.cadastrarPartida(partidaRequestDTO)
        );
        assertEquals("Estádio não existe!", ex.getMessage());
    }

    @Test
    void cadastrarPartidaClubeCasaInativo() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), false);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                casa.getId(), visitante.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 21, 23, 0), 1, 1);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.cadastrarPartida(partidaRequestDTO)
        );
        assertEquals("Clube da casa está inativo.", ex.getMessage());
    }

    @Test
    void cadastrarPartidaClubeVisitanteInativo() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), false);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                casa.getId(), visitante.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 22, 10, 0), 1, 1);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.cadastrarPartida(partidaRequestDTO)
        );
        assertEquals("Clube visitante está inativo.", ex.getMessage());
    }

    @Test
    void cadastrarPartidaDataAntesDeCriacaoDoClube() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(2024, 8, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(2014, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                casa.getId(), visitante.getId(), estadio.getId(),
                LocalDateTime.of(2024, 7, 20, 21, 0), 1, 1);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.cadastrarPartida(partidaRequestDTO)
        );
        assertEquals("Data da partida não pode ser anterior à data de criação de algum clube.", ex.getMessage());
    }

    @Test
    void cadastrarPartidaMenosDe48hIntervalo() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(casa, visitante, estadio, LocalDateTime.of(2025, 7, 23, 21, 0), 1, 1);

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                casa.getId(), visitante.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 24, 20, 0), 1, 1);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.cadastrarPartida(partidaRequestDTO)
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

        PartidaRequestDTO  partidaRequestDTO= criarPartidaDTO(
                casa2.getId(), visitante2.getId(), estadio.getId(),
                LocalDateTime.of(2025, 7, 25, 21, 0), 2, 2);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.cadastrarPartida(partidaRequestDTO)
        );
        assertEquals("Já existe partida nesse estádio nesse dia!", ex.getMessage());
    }

    @Test
    void atualizarPartidaComSucesso() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(c1, c2, estadio,
                LocalDateTime.of(2025, 8, 1, 16, 0), 1, 1);

        Estadio estadioNovo = criarSalvarEstadio("Allianz Parque");
        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                c1.getId(), c2.getId(), estadioNovo.getId(),
                LocalDateTime.of(2025, 8, 2, 18, 0), 3, 2);

        String msg = partidaService.atualizarPartida(partida.getId(), partidaRequestDTO);

        assertTrue(msg.contains("Partida atualizada"));
        Partida atualizada = partidaRepository.findById(partida.getId()).orElse(null);
        assertNotNull(atualizada);
        assertEquals(estadioNovo.getId(), atualizada.getEstadio().getId());
        assertEquals(3, atualizada.getGolsCasa());
        assertEquals(2, atualizada.getGolsVisitante());
    }

    @Test
    void atualizarPartidaNaoEncontrada() {
        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(1L, 2L, 3L, LocalDateTime.now().plusDays(10), 0, 0);
        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.atualizarPartida(99L, partidaRequestDTO)
        );
        assertEquals("Partida não encontrada!", ex.getMessage());
    }

    @Test
    void atualizarPartidaCamposObrigatorios() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(c1, c2, estadio, LocalDateTime.now().plusDays(3), 1, 1);

        PartidaRequestDTO partidaRequestDTO = new PartidaRequestDTO();
        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.atualizarPartida(partida.getId(), partidaRequestDTO)
        );
        assertEquals("Todos os campos são obrigatórios.", ex.getMessage());
    }

    @Test
    void atualizarPartidaClubesIguais() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(c1, c1, estadio, LocalDateTime.now().plusDays(2), 1, 1);

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                c1.getId(), c1.getId(), estadio.getId(), LocalDateTime.now().plusDays(3), 2, 2);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.atualizarPartida(partida.getId(), partidaRequestDTO)
        );
        assertEquals("Clubes não podem ser iguais!", ex.getMessage());
    }

    @Test
    void atualizarPartidaGolsNegativos() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(c1, c2, estadio, LocalDateTime.now().plusDays(3), 1, 2);

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                c1.getId(), c2.getId(), estadio.getId(), LocalDateTime.now().plusDays(4), -1, 2);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.atualizarPartida(partida.getId(), partidaRequestDTO)
        );
        assertEquals("Gols não podem ser negativos!", ex.getMessage());
    }

    @Test
    void atualizarPartidaClubeCasaNaoExiste() {
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(visitante, visitante, morumbi, LocalDateTime.now().plusDays(4), 1, 1);

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                99L, visitante.getId(), morumbi.getId(), LocalDateTime.now().plusDays(5), 3, 1);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.atualizarPartida(partida.getId(), partidaRequestDTO)
        );
        assertEquals("Clube da casa não existe!", ex.getMessage());
    }

    @Test
    void atualizarPartidaClubeVisitanteNaoExiste() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(corinthians, corinthians, morumbi, LocalDateTime.now().plusDays(4), 1, 1);

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                corinthians.getId(), 99L, morumbi.getId(), LocalDateTime.now().plusDays(5), 2, 2);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.atualizarPartida(partida.getId(), partidaRequestDTO)
        );
        assertEquals("Clube visitante não existe!", ex.getMessage());
    }

    @Test
    void atualizarPartidaEstadioNaoExiste() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(c1, c2, estadio, LocalDateTime.now().plusDays(4), 1, 2);

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                c1.getId(), c2.getId(), 99L, LocalDateTime.now().plusDays(5), 2, 2);

        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.atualizarPartida(partida.getId(), partidaRequestDTO)
        );
        assertEquals("Estádio não existe!", ex.getMessage());
    }

    @Test
    void atualizarPartidaClubeCasaInativo() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), false);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(corinthians, palmeiras, morumbi, LocalDateTime.now().plusDays(4), 1, 2);

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                corinthians.getId(), palmeiras.getId(), morumbi.getId(), LocalDateTime.now().plusDays(5), 2, 2);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.atualizarPartida(partida.getId(), partidaRequestDTO)
        );
        assertEquals("Clube da casa está inativo.", ex.getMessage());
    }

    @Test
    void atualizarPartidaClubeVisitanteInativo() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), false);
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(corinthians, palmeiras, morumbi, LocalDateTime.now().plusDays(4), 1, 2);

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                corinthians.getId(), palmeiras.getId(), morumbi.getId(), LocalDateTime.now().plusDays(5), 2, 2);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.atualizarPartida(partida.getId(), partidaRequestDTO)
        );
        assertEquals("Clube visitante está inativo.", ex.getMessage());
    }

    @Test
    void atualizarPartidaDataAntesDeCriacaoDoClube() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(2025, 10, 1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(corinthians, palmeiras, morumbi, LocalDateTime.now().plusDays(10), 1, 2);

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                corinthians.getId(), palmeiras.getId(), morumbi.getId(),
                LocalDateTime.of(2025, 9, 20, 20, 0), 2, 2);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.atualizarPartida(partida.getId(), partidaRequestDTO)
        );
        assertEquals("Data da partida não pode ser anterior à data de criação de algum clube.", ex.getMessage());
    }

    @Test
    void atualizarPartidaMenosDe48hIntervalo() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");

        Partida partida = criarSalvarPartida(
                corinthians, palmeiras, morumbi,
                LocalDateTime.of(2025, 8, 20, 16, 0), 1, 1
        );

        criarSalvarPartida(
                corinthians, palmeiras, morumbi,
                LocalDateTime.of(2025, 8, 19, 16, 0), 1, 1
        );


        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                corinthians.getId(), palmeiras.getId(), morumbi.getId(),
                LocalDateTime.of(2025, 8, 21, 10, 0), 2, 2
        );

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.atualizarPartida(partida.getId(), partidaRequestDTO)
        );
        assertEquals("Um dos clubes já tem partida marcada em menos de 48h!", ex.getMessage());
    }

    @Test
    void atualizarPartidaEstadioOcupadoMesmoDia() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(corinthians, palmeiras, morumbi, LocalDateTime.now().plusDays(7), 1, 1);

        Clube flamengo = criarSalvarClube("Flamengo", "RJ", LocalDate.of(1931, 1, 1), true);
        Clube gremio = criarSalvarClube("Gremio", "RS", LocalDate.of(1903, 9, 15), true);
        criarSalvarPartida(flamengo, gremio, morumbi, LocalDateTime.now().plusDays(7).withHour(21), 2, 2);

        PartidaRequestDTO partidaRequestDTO = criarPartidaDTO(
                corinthians.getId(), palmeiras.getId(), morumbi.getId(),
                LocalDateTime.now().plusDays(7).withHour(20), 2, 2);

        Exception ex = assertThrows(PartidaCadastroException.class, () ->
                partidaService.atualizarPartida(partida.getId(), partidaRequestDTO)
        );
        assertEquals("Já existe partida nesse estádio nesse dia!", ex.getMessage());
    }

    @Test
    void removerPartidaComSucesso() {

        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        Partida partida = criarSalvarPartida(
                casa, visitante, estadio, LocalDateTime.of(2025, 7, 20, 21, 0), 2, 2
        );
        Long partidaId = partida.getId();

        partidaService.removerPartida(partidaId);

        assertFalse(partidaRepository.findById(partidaId).isPresent());
    }

    @Test
    void removerPartidaNaoEncontrada() {
        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.removerPartida(99L)
        );
        assertEquals("Partida não encontrada!", ex.getMessage());
    }

    @Test
    void buscarPartidaComSucesso() {

        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        Partida partida = criarSalvarPartida(
                casa, visitante, estadio, LocalDateTime.of(2025, 7, 20, 21, 0), 2, 2
        );

        Partida resultado = partidaService.buscarPartida(partida.getId());

        assertNotNull(resultado);
        assertEquals(partida.getId(), resultado.getId());
        assertEquals("Corinthians", resultado.getClubeCasa().getNome());
        assertEquals("Palmeiras", resultado.getClubeVisitante().getNome());
    }

    @Test
    void buscarPartidaNaoEncontrada() {
        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                partidaService.buscarPartida(999L)
        );
        assertEquals("Partida não encontrada", ex.getMessage());
    }

    @Test
    void listarTodasPartidasSemFiltro() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(casa, visitante, estadio, LocalDateTime.of(2025, 8, 1, 16, 0), 2, 1);
        criarSalvarPartida(visitante, casa, estadio, LocalDateTime.of(2025, 8, 2, 20, 0), 1, 1);

        Page<Partida> page = partidaService.listarPartidas(null,null,null,null,PageRequest.of(0,10));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void listarPartidasPorClubeMandanteNull() {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(casa, visitante, estadio, LocalDateTime.of(2025,8,1,20,0), 3,1);
        criarSalvarPartida(visitante, casa, estadio, LocalDateTime.of(2025,8,2,22,0), 2,3);

        Page<Partida> page = partidaService.listarPartidas(casa.getId(), null, null, null, PageRequest.of(0,10));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void listarPartidasPorClubeMandanteTrue() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,20,0), 3,1);
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,2,22,0), 2,3);

        Page<Partida> page = partidaService.listarPartidas(corinthians.getId(), null, null, true, PageRequest.of(0,10));

        assertEquals(1, page.getTotalElements());
        assertEquals(3, page.getContent().get(0).getGolsCasa());
    }

    @Test
    void listarPartidasPorClubeMandanteFalse() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,20,0), 3,1);
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,2,22,0), 2,3);

        Page<Partida> page = partidaService.listarPartidas(corinthians.getId(), null, null, false, PageRequest.of(0,10));

        assertEquals(1, page.getTotalElements());
        assertEquals(3, page.getContent().get(0).getGolsVisitante());
    }

    @Test
    void listarPartidasPorEstadio() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        Estadio allianz = criarSalvarEstadio("Allianz Parque");

        criarSalvarPartida(c1, c2, morumbi, LocalDateTime.of(2025,8,1,20,0), 2,2);
        criarSalvarPartida(c2, c1, allianz, LocalDateTime.of(2025,8,2,22,0), 1,1);

        Page<Partida> morumbiPage = partidaService.listarPartidas(null, morumbi.getId(), null, null, PageRequest.of(0,10));
        assertEquals(1, morumbiPage.getTotalElements());
        assertEquals(morumbi.getId(), morumbiPage.getContent().get(0).getEstadio().getId());
    }

    @Test
    void listarPartidasGoleada() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,8,1,20,0), 5,1); // goleada!
        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,8,2,22,0), 1,2); // não-goeada

        Page<Partida> goleadasPage = partidaService.listarPartidas(null, null, true, null, PageRequest.of(0,10));
        assertEquals(1, goleadasPage.getTotalElements());
        assertEquals(5, goleadasPage.getContent().get(0).getGolsCasa());
    }

    @Test
    void listarPartidasCombinandoTodosFiltros() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        criarSalvarPartida(c1, c2, morumbi, LocalDateTime.of(2025,8,1,20,0), 4,1);

        Page<Partida> page = partidaService.listarPartidas(
                c1.getId(), morumbi.getId(), true, true, PageRequest.of(0,10));

        assertEquals(1, page.getTotalElements());
    }

    @Test
    void listarPartidasNenhumResultado() {
        Page<Partida> page = partidaService.listarPartidas(999L, 999L, true, true, PageRequest.of(0,10));
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void listarPartidasPaginacao() {

        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");


        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025, 8, 1, 20, 0), 1, 1);
        criarSalvarPartida(c2, c1, estadio, LocalDateTime.of(2025, 8, 2, 21, 0), 2, 1);
        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025, 8, 3, 22, 0), 0, 3);


        Page<Partida> page1 = partidaService.listarPartidas(null, null, null, null, PageRequest.of(0, 2));
        assertEquals(2, page1.getContent().size());

        Page<Partida> page2 = partidaService.listarPartidas(null, null, null, null, PageRequest.of(1, 2));
        assertEquals(1, page2.getContent().size());
        assertEquals(3, page1.getTotalElements());
    }

}
