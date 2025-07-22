package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.ClubeRankingDTO;
import campeonato.com.Campeonato.dto.RetrospectoClubeDTO;
import campeonato.com.Campeonato.dto.RetrospectoContraDTO;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RelatorioServiceTest {

    @Autowired
    private RelatorioService relatorioService;

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
    void retrospectoClubeSucesso() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, morumbi, LocalDateTime.of(2025,8,1,21,0), 2, 1);
        criarSalvarPartida(palmeiras, corinthians, morumbi, LocalDateTime.of(2025,8,2,21,0), 1, 1);
        criarSalvarPartida(palmeiras, corinthians, morumbi, LocalDateTime.of(2025,8,3,21,0), 3, 2);

        RetrospectoClubeDTO retro = relatorioService.retrospectoClube(corinthians.getId());
        assertEquals(1, retro.getVitorias());
        assertEquals(1, retro.getEmpates());
        assertEquals(1, retro.getDerrotas());
        assertEquals(5, retro.getGolsFeitos());
        assertEquals(5, retro.getGolsSofridos());
    }

    @Test
    void retrospectoClubeSemPartidas() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);

        RetrospectoClubeDTO retro = relatorioService.retrospectoClube(corinthians.getId());
        assertEquals(0, retro.getVitorias());
        assertEquals(0, retro.getEmpates());
        assertEquals(0, retro.getDerrotas());
        assertEquals(0, retro.getGolsFeitos());
        assertEquals(0, retro.getGolsSofridos());
    }

    @Test
    void retrospectoClubeNaoEncontrado() {
        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                relatorioService.retrospectoClube(9999L)
        );
        assertEquals("Clube não encontrado!", ex.getMessage());
    }

    @Test
    void retrospectoContraAdversarios_Sucesso() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Clube gremio = criarSalvarClube("Gremio", "RS", LocalDate.of(1903, 9, 15), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");


        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,21,0), 2, 1);
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,5,21,0), 1, 1);
        criarSalvarPartida(gremio, corinthians, estadio, LocalDateTime.of(2025,8,10,21,0), 2, 0);

        List<RetrospectoContraDTO> lista = relatorioService.retrospectoContraAdversarios(corinthians.getId());
        assertEquals(2, lista.size());

        RetrospectoContraDTO contraPalmeiras = lista.stream()
                .filter(r -> "Palmeiras".equals(r.getAdversarioNome()))
                .findFirst().orElse(null);
        assertNotNull(contraPalmeiras);
        assertEquals(1, contraPalmeiras.getVitorias());
        assertEquals(1, contraPalmeiras.getEmpates());
        assertEquals(0, contraPalmeiras.getDerrotas());

        RetrospectoContraDTO contraGremio = lista.stream()
                .filter(r -> "Gremio".equals(r.getAdversarioNome()))
                .findFirst().orElse(null);
        assertNotNull(contraGremio);
        assertEquals(0, contraGremio.getVitorias());
        assertEquals(0, contraGremio.getEmpates());
        assertEquals(1, contraGremio.getDerrotas());
        assertEquals(0, contraGremio.getGolsFeitos());
        assertEquals(2, contraGremio.getGolsSofridos());
    }

    @Test
    void retrospectoContraAdversariosSemPartidas() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);

        List<RetrospectoContraDTO> lista = relatorioService.retrospectoContraAdversarios(corinthians.getId());
        assertTrue(lista.isEmpty());
    }

    @Test
    void retrospectoContraAdversarios_ClubeNaoExiste() {
        Exception ex = assertThrows(PartidaValidacaoException.class, () ->
                relatorioService.retrospectoContraAdversarios(9999L)
        );
        assertEquals("Clube não encontrado!", ex.getMessage());
    }

    @Test
    void confrontosEntreClubesComSucesso() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,10,21,0), 3, 1);

        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,9,10,21,0), 2, 2);

        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,10,10,21,0), 1, 0);

        Map<String, Object> resultado = relatorioService.confrontosEntreClubes(corinthians.getId(), palmeiras.getId());

        List<Partida> partidas = (List<Partida>) resultado.get("partidas");
        assertEquals(3, partidas.size());

        RetrospectoContraDTO retroA = (RetrospectoContraDTO) resultado.get("retrospectoA");
        RetrospectoContraDTO retroB = (RetrospectoContraDTO) resultado.get("retrospectoB");

        assertEquals(1, retroA.getVitorias());
        assertEquals(1, retroA.getEmpates());
        assertEquals(1, retroA.getDerrotas());

        assertEquals(1, retroB.getVitorias());
        assertEquals(1, retroB.getEmpates());
        assertEquals(1, retroB.getDerrotas());
    }

    @Test
    void confrontosEntreClubesNenhumJogo() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);

        Map<String, Object> resultado = relatorioService.confrontosEntreClubes(corinthians.getId(), palmeiras.getId());

        List<Partida> partidas = (List<Partida>) resultado.get("partidas");
        assertTrue(partidas.isEmpty());

        RetrospectoContraDTO retroA = (RetrospectoContraDTO) resultado.get("retrospectoA");
        assertEquals(0, retroA.getVitorias());
        assertEquals(0, retroA.getEmpates());
        assertEquals(0, retroA.getDerrotas());
        assertEquals(0, retroA.getGolsFeitos());
        assertEquals(0, retroA.getGolsSofridos());
    }

    @Test
    void confrontosEntreClubesClubeANaoExiste() {
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Exception ex = assertThrows(ResponseStatusException.class, () ->
                relatorioService.confrontosEntreClubes(9999L, palmeiras.getId())
        );
        assertTrue(ex.getMessage().contains("Clube A não encontrado!"));
    }

    @Test
    void confrontosEntreClubesClubeBNaoExiste() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Exception ex = assertThrows(ResponseStatusException.class, () ->
                relatorioService.confrontosEntreClubes(corinthians.getId(), 9999L)
        );
        assertTrue(ex.getMessage().contains("Clube B não encontrado!"));
    }

    @Test
    void confrontosEntreClubesMandanteEVisitanteAmbosLados() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio est = criarSalvarEstadio("Morumbi");


        criarSalvarPartida(corinthians, palmeiras, est, LocalDateTime.of(2025, 8, 1, 20, 0), 2, 1);

        criarSalvarPartida(palmeiras, corinthians, est, LocalDateTime.of(2025, 8, 2, 20, 0), 3, 2);

        Map<String, Object> resultado = relatorioService.confrontosEntreClubes(corinthians.getId(), palmeiras.getId());
        List<Partida> partidas = (List<Partida>) resultado.get("partidas");
        assertEquals(2, partidas.size());
    }

    @Test
    void confrontosEntreClubesAMandanteBVisitante() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio est = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, est, LocalDateTime.of(2025, 8, 1, 20, 0), 2, 1);

        Map<String, Object> resultado = relatorioService.confrontosEntreClubes(corinthians.getId(), palmeiras.getId());
        List<Partida> partidas = (List<Partida>) resultado.get("partidas");
        assertEquals(1, partidas.size());
        assertEquals("Corinthians", partidas.get(0).getClubeCasa().getNome());
        assertEquals("Palmeiras", partidas.get(0).getClubeVisitante().getNome());
    }

    @Test
    void confrontosEntreClubesBMandanteAVisitante() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio est = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(palmeiras, corinthians, est, LocalDateTime.of(2025, 8, 2, 20, 0), 3, 2);

        Map<String, Object> resultado = relatorioService.confrontosEntreClubes(corinthians.getId(), palmeiras.getId());
        List<Partida> partidas = (List<Partida>) resultado.get("partidas");
        assertEquals(1, partidas.size());
        assertEquals("Palmeiras", partidas.get(0).getClubeCasa().getNome());
        assertEquals("Corinthians", partidas.get(0).getClubeVisitante().getNome());
    }

    @Test
    void rankingPorPontosComSucesso() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,7,20,21,0), 3, 1); // Corinthians win
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,10,21,0), 2, 2); // empate

        List<ClubeRankingDTO> rank = relatorioService.ranking("pontos");
        assertEquals(2, rank.size());
        assertEquals("Corinthians", rank.get(0).getNome());
        assertEquals(4, rank.get(0).getPontos()); // 3(win) + 1(empate)
        assertEquals("Palmeiras", rank.get(1).getNome());
        assertEquals(1, rank.get(1).getPontos());
    }

    @Test
    void rankingPorGols() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,7,20,21,0), 5, 1); // Corinthians: 5, Palmeiras: 1
        criarSalvarPartida(c2, c1, estadio, LocalDateTime.of(2025,8,10,21,0), 2, 2);

        List<ClubeRankingDTO> rank = relatorioService.ranking("gols");
        assertEquals("Corinthians", rank.get(0).getNome());
        assertEquals(7, rank.get(0).getGolsFeitos()); // 5 + 2
        assertEquals("Palmeiras", rank.get(1).getNome());
        assertEquals(3, rank.get(1).getGolsFeitos()); // 1 + 2
    }

    @Test
    void rankingPorVitorias() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,7,20,21,0), 2, 1); // Cor win
        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,8,10,21,0), 2, 0); // Cor win
        criarSalvarPartida(c2, c1, estadio, LocalDateTime.of(2025,9,10,21,0), 2, 0); // Pal win

        List<ClubeRankingDTO> rank = relatorioService.ranking("vitorias");
        assertEquals("Corinthians", rank.get(0).getNome());
        assertEquals(2, rank.get(0).getVitorias());
        assertEquals("Palmeiras", rank.get(1).getNome());
        assertEquals(1, rank.get(1).getVitorias());
    }

    @Test
    void rankingPorJogos() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,7,20,21,0), 1, 2);
        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,8,10,21,0), 2, 2);

        List<ClubeRankingDTO> rank = relatorioService.ranking("jogos");
        assertEquals(2, rank.size());
        assertEquals("Corinthians", rank.get(0).getNome());
        assertEquals(2, rank.get(0).getJogos());
        assertEquals("Palmeiras", rank.get(1).getNome());
        assertEquals(2, rank.get(1).getJogos());
    }

    @Test
    void rankingSemClubesDePonto() {

        List<ClubeRankingDTO> rank = relatorioService.ranking("pontos");
        assertTrue(rank.isEmpty());
    }

    @Test
    void rankingCriterioInvalido() {

        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);

        List<ClubeRankingDTO> rank = relatorioService.ranking("banana");
        assertTrue(rank.isEmpty());
    }

    @Test
    void partidasGoleadaSemFiltro() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,8,1,19,0), 2, 0);

        criarSalvarPartida(c2, c1, estadio, LocalDateTime.of(2025,8,2,20,0), 5, 1);

        Page<Partida> page = relatorioService.partidasGoleada(null, null, null, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals(5, page.getContent().get(0).getGolsCasa());
    }

    @Test
    void partidasGoleadaFiltroClubeMandante() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,19,0), 4, 0); // Corinthians, mandante, goleada
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,2,20,0), 2, 1);


        Page<Partida> page = relatorioService.partidasGoleada(corinthians.getId(), null, true, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Corinthians", page.getContent().get(0).getClubeCasa().getNome());
    }

    @Test
    void partidasGoleadaFiltroClubeVisitante() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,2,20,0), 0, 3); // Corinthians visitante goleada

        Page<Partida> page = relatorioService.partidasGoleada(corinthians.getId(), null, false, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Corinthians", page.getContent().get(0).getClubeVisitante().getNome());
    }

    @Test
    void partidasGoleadasPorEstadio() {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio1 = criarSalvarEstadio("Morumbi");
        Estadio estadio2 = criarSalvarEstadio("Allianz Parque");
        criarSalvarPartida(c1, c2, estadio1, LocalDateTime.of(2025,8,1,19,0), 5, 1);
        criarSalvarPartida(c2, c1, estadio2, LocalDateTime.of(2025,8,2,20,0), 6, 2);

        Page<Partida> page = relatorioService.partidasGoleada(null, estadio1.getId(), null, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals(estadio1.getId(), page.getContent().get(0).getEstadio().getId());
    }

    @Test
    void partidasGoleadaNadaEncontrado() {
        Page<Partida> page = relatorioService.partidasGoleada(999L, null, null, PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void partidasPorClubeTodosFiltros() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        Estadio allianz = criarSalvarEstadio("Allianz Parque");

        criarSalvarPartida(corinthians, palmeiras, morumbi, LocalDateTime.of(2025,8,1,19,0), 4, 1);
        criarSalvarPartida(palmeiras, corinthians, morumbi, LocalDateTime.of(2025,8,2,20,0), 2, 2);
        criarSalvarPartida(corinthians, palmeiras, allianz, LocalDateTime.of(2025,8,4,21,0), 5, 0);


        Page<Partida> page = relatorioService.partidasPorClube(corinthians.getId(), morumbi.getId(), true, true, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals(4, page.getContent().get(0).getGolsCasa());
    }

    @Test
    void partidasPorClubePaginacao() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025, 8, 1, 20, 0), 2, 1);
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025, 8, 2, 20, 0), 3, 3);
        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025, 8, 3, 20, 0), 6, 0);

        Page<Partida> page1 = relatorioService.partidasPorClube(corinthians.getId(), null, null, null, PageRequest.of(0, 2));
        assertEquals(2, page1.getContent().size());
        Page<Partida> page2 = relatorioService.partidasPorClube(corinthians.getId(), null, null, null, PageRequest.of(1, 2));
        assertEquals(1, page2.getContent().size());
        assertEquals(3, page1.getTotalElements());
    }

    @Test
    void partidasPorClubeNadaEncontrado() {
        Page<Partida> page = relatorioService.partidasPorClube(999L, null, null, null, PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }
}
