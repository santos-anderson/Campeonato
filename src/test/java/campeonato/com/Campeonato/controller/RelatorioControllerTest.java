package campeonato.com.Campeonato.controller;


import campeonato.com.Campeonato.entity.Clube;
import campeonato.com.Campeonato.entity.Estadio;
import campeonato.com.Campeonato.entity.Partida;
import campeonato.com.Campeonato.repository.ClubeRepository;
import campeonato.com.Campeonato.repository.EstadioRepository;
import campeonato.com.Campeonato.repository.PartidaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    private Partida criarSalvarPartida(Clube casa, Clube visitante, Estadio estadio, LocalDateTime dataHora, int golsCasa, int golsVisitante) {
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
    void retrospectoClubeComSucesso() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");


        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,21,0), 2, 1);
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,2,21,0), 1, 1);
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,3,21,0), 3, 2);

        mockMvc.perform(get("/relatorios/retrospecto/" + corinthians.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vitorias").value(1))
                .andExpect(jsonPath("$.empates").value(1))
                .andExpect(jsonPath("$.derrotas").value(1))
                .andExpect(jsonPath("$.golsFeitos").value(5))    // 2+1+2
                .andExpect(jsonPath("$.golsSofridos").value(5)); // 1+1+3
    }

    @Test
    void retrospectoClubeSemPartidas() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);

        mockMvc.perform(get("/relatorios/retrospecto/" + corinthians.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vitorias").value(0))
                .andExpect(jsonPath("$.empates").value(0))
                .andExpect(jsonPath("$.derrotas").value(0))
                .andExpect(jsonPath("$.golsFeitos").value(0))
                .andExpect(jsonPath("$.golsSofridos").value(0));
    }

    @Test
    void retrospectoClubeNaoEncontrado() throws Exception {
        mockMvc.perform(get("/relatorios/retrospecto/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Clube não encontrado!"));
    }

    @Test
    void retrospectoContraComAdversario() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,21,0), 2, 1);
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,2,21,0), 1, 1);

        mockMvc.perform(get("/relatorios/retrospecto-contra/" + corinthians.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].adversarioNome").value("Palmeiras"))
                .andExpect(jsonPath("$[0].vitorias").value(1)) // Corinthians ganhou uma
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void retrospectoContraSemPartidas() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);

        mockMvc.perform(get("/relatorios/retrospecto-contra/" + corinthians.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void retrospectoContraClubeNaoEncontrado() throws Exception {
        mockMvc.perform(get("/relatorios/retrospecto-contra/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Clube não encontrado!"));
    }

    @Test
    void confrontosEntreDoisClubesSucesso() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,19,0), 2, 0);

        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,2,19,0), 3, 2);

        mockMvc.perform(get("/relatorios/confrontos")
                        .param("clubeA", corinthians.getId().toString())
                        .param("clubeB", palmeiras.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partidas.length()").value(2))
                .andExpect(jsonPath("$.retrospectoA.adversarioNome").value("Palmeiras"))
                .andExpect(jsonPath("$.retrospectoB.adversarioNome").value("Corinthians"))
                .andExpect(jsonPath("$.retrospectoA.vitorias").value(1)) // Corinthians venceu 1
                .andExpect(jsonPath("$.retrospectoB.vitorias").value(1)); // Palmeiras venceu 1
    }

    @Test
    void confrontosEntreClubesSemPartidas() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);

        mockMvc.perform(get("/relatorios/confrontos")
                        .param("clubeA", c1.getId().toString())
                        .param("clubeB", c2.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partidas.length()").value(0))
                .andExpect(jsonPath("$.retrospectoA.vitorias").value(0))
                .andExpect(jsonPath("$.retrospectoA.empates").value(0))
                .andExpect(jsonPath("$.retrospectoA.derrotas").value(0))
                .andExpect(jsonPath("$.retrospectoB.vitorias").value(0))
                .andExpect(jsonPath("$.retrospectoB.empates").value(0))
                .andExpect(jsonPath("$.retrospectoB.derrotas").value(0));
    }

    @Test
    void confrontosClubeANaoExiste() throws Exception {
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);

        mockMvc.perform(get("/relatorios/confrontos")
                        .param("clubeA", "9999")
                        .param("clubeB", c2.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void confrontosClubeBNaoExiste() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);

        mockMvc.perform(get("/relatorios/confrontos")
                        .param("clubeA", c1.getId().toString())
                        .param("clubeB", "9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rankingPorPontos() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");


        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,7,20,21,0), 2, 1);
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,10,21,0), 1, 1);

        mockMvc.perform(get("/relatorios/ranking?criterio=pontos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Corinthians"))
                .andExpect(jsonPath("$[0].pontos").value(4))
                .andExpect(jsonPath("$[1].nome").value("Palmeiras"))
                .andExpect(jsonPath("$[1].pontos").value(1));
    }

    @Test
    void rankingPorGols() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,7,20,21,0), 5, 1); // Corinthians 5g, Palmeiras 1g
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,10,21,0), 2, 2); // Ambos 2g

        mockMvc.perform(get("/relatorios/ranking?criterio=gols"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Corinthians"))
                .andExpect(jsonPath("$[0].golsFeitos").value(7))  // 5+2
                .andExpect(jsonPath("$[1].nome").value("Palmeiras"))
                .andExpect(jsonPath("$[1].golsFeitos").value(3)); // 1+2
    }

    @Test
    void rankingPorVitorias() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,7,20,21,0), 1, 0); // Cor win
        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,8,10,21,0), 2, 0); // Cor win
        criarSalvarPartida(c2, c1, estadio, LocalDateTime.of(2025,9,10,21,0), 1, 0); // Palmeiras win

        mockMvc.perform(get("/relatorios/ranking?criterio=vitorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vitorias").value(2))
                .andExpect(jsonPath("$[1].vitorias").value(1));
    }

    @Test
    void rankingPorJogos() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,7,20,21,0), 2, 2);
        criarSalvarPartida(c2, c1, estadio, LocalDateTime.of(2025,8,10,21,0), 3, 3);

        mockMvc.perform(get("/relatorios/ranking?criterio=jogos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jogos").value(2))
                .andExpect(jsonPath("$[1].jogos").value(2));
    }

    @Test
    void rankingSemClubesParaOCriterio() throws Exception {
        mockMvc.perform(get("/relatorios/ranking?criterio=pontos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rankingCriterioInvalido() throws Exception {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        mockMvc.perform(get("/relatorios/ranking?criterio=invalido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void goleadasSemFiltro() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio est = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(c1, c2, est, LocalDateTime.of(2025,8,1,20,0), 5, 1); // goleada
        criarSalvarPartida(c2, c1, est, LocalDateTime.of(2025,8,2,20,0), 2, 2); // não goleada

        mockMvc.perform(get("/relatorios/goleadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].golsCasa").value(5));
    }

    @Test
    void goleadasFiltroPorClubeMandante() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,21,0), 4, 0); // Corinthians mandante, goleada

        mockMvc.perform(get("/relatorios/goleadas")
                        .param("clubeId", corinthians.getId() + "")
                        .param("mandante", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].clubeCasa.nome").value("Corinthians"));
    }

    @Test
    void goleadasFiltroPorClubeVisitanteSemResultado() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,21,0), 2, 1);

        mockMvc.perform(get("/relatorios/goleadas")
                        .param("clubeId", palmeiras.getId().toString())
                        .param("mandante", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void goleadasFiltroPorEstadio() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadioA = criarSalvarEstadio("Morumbi");
        Estadio estadioB = criarSalvarEstadio("Allianz Parque");
        criarSalvarPartida(corinthians, palmeiras, estadioA, LocalDateTime.of(2025,8,1,21,0), 4, 0);
        criarSalvarPartida(palmeiras, corinthians, estadioB, LocalDateTime.of(2025,8,10,21,0), 6, 1);

        mockMvc.perform(get("/relatorios/goleadas")
                        .param("estadioId", estadioA.getId()+""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].estadio.nome").value("Morumbi"));
    }

    @Test
    void partidasClubeSemFiltros() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,21,0), 2, 0); // Corinthians mandante
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,2,21,0), 1, 1); // Corinthians visitante

        mockMvc.perform(get("/relatorios/partidas-clube")
                        .param("clubeId", corinthians.getId() + ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void partidasClubeSomenteMandante() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,21,0), 4, 0); // Corinthians mandante, goleada
        criarSalvarPartida(palmeiras, corinthians, estadio, LocalDateTime.of(2025,8,2,21,0), 1, 1);

        mockMvc.perform(get("/relatorios/partidas-clube")
                        .param("clubeId", corinthians.getId() + "")
                        .param("mandante", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].clubeCasa.nome").value("Corinthians"));
    }

    @Test
    void partidasClubeMandanteGoleada() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,21,0), 5, 2); // Goleada
        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,2,21,0), 2, 1);

        mockMvc.perform(get("/relatorios/partidas-clube")
                        .param("clubeId", corinthians.getId() + "")
                        .param("mandante", "true")
                        .param("goleada", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].golsCasa").value(5));
    }

    @Test
    void partidasClubeSemResultado() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        mockMvc.perform(get("/relatorios/partidas-clube")
                        .param("clubeId", corinthians.getId() + "")
                        .param("mandante", "true")
                        .param("goleada", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void rankingParametroDefault() throws Exception {
        Clube c = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio e = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(c, c2, e, LocalDateTime.now(), 2, 0); // Corinthians ganha

        mockMvc.perform(get("/relatorios/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Corinthians"))
                .andExpect(jsonPath("$[0].pontos").value(3));
    }

    @Test
    void rankingSemNenhumJogo() throws Exception {

        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        mockMvc.perform(get("/relatorios/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void goleadasComPaginacao() throws Exception {
        Clube c1 = criarSalvarClube("Time1", "SP", LocalDate.of(1900, 1, 1), true);
        Clube c2 = criarSalvarClube("Time2", "SP", LocalDate.of(1900, 1, 1), true);
        Estadio est = criarSalvarEstadio("Estadio Teste");
        for (int i = 0; i < 3; i++) criarSalvarPartida(c1, c2, est, LocalDateTime.now().plusDays(i), 4, 0);
        mockMvc.perform(get("/relatorios/goleadas").param("size", "2").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
        mockMvc.perform(get("/relatorios/goleadas").param("size", "2").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void partidasClubeComPaginacao() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio est = criarSalvarEstadio("Morumbi");
        for (int i = 0; i < 3; i++) criarSalvarPartida(c1, c2, est, LocalDateTime.now().plusDays(i), 2, 0);
        mockMvc.perform(get("/relatorios/partidas-clube")
                        .param("clubeId", c1.getId().toString())
                        .param("size", "2").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
        mockMvc.perform(get("/relatorios/partidas-clube")
                        .param("clubeId", c1.getId().toString())
                        .param("size", "2").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void confrontosSemClubeA() throws Exception {
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        mockMvc.perform(get("/relatorios/confrontos")
                        .param("clubeB", c2.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confrontosSemClubeB() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        mockMvc.perform(get("/relatorios/confrontos")
                        .param("clubeA", c1.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confrontosSemNenhumParametro() throws Exception {
        mockMvc.perform(get("/relatorios/confrontos"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retrospectoIdInvalido() throws Exception {
        mockMvc.perform(get("/relatorios/retrospecto/abc"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void retrospectoContraIdInvalido() throws Exception {
        mockMvc.perform(get("/relatorios/retrospecto-contra/abc"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void partidasClubeIdInvalido() throws Exception {
        mockMvc.perform(get("/relatorios/partidas-clube").param("clubeId", "abc"))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void goleadasTodosFiltros() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio est = criarSalvarEstadio("Morumbi");
        criarSalvarPartida(c1, c2, est, LocalDateTime.now(), 4, 0);
        mockMvc.perform(get("/relatorios/goleadas")
                        .param("clubeId", c1.getId().toString())
                        .param("estadioId", est.getId().toString())
                        .param("mandante", "true")
                        .param("size", "1")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
    @Test
    void partidasClubeTodosFiltros() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio est = criarSalvarEstadio("Morumbi");
        criarSalvarPartida(c1, c2, est, LocalDateTime.now(), 5, 2);
        mockMvc.perform(get("/relatorios/partidas-clube")
                        .param("clubeId", c1.getId().toString())
                        .param("estadioId", est.getId().toString())
                        .param("goleada", "true")
                        .param("mandante", "true")
                        .param("size", "1")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }


}

