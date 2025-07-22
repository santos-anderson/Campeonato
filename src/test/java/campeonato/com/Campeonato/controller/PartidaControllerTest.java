package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.entity.Clube;
import campeonato.com.Campeonato.entity.Estadio;
import campeonato.com.Campeonato.entity.Partida;
import campeonato.com.Campeonato.repository.ClubeRepository;
import campeonato.com.Campeonato.repository.EstadioRepository;
import campeonato.com.Campeonato.repository.PartidaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PartidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClubeRepository clubeRepository;

    @Autowired
    private EstadioRepository estadioRepository;

    @Autowired
    PartidaRepository partidaRepository;


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
    void cadastrarPartidaComSucesso() throws Exception {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        String json = """
            {
                "clubeCasaId": %d,
                "clubeVisitanteId": %d,
                "estadioId": %d,
                "dataHora": "2025-08-10T19:00:00",
                "golsCasa": 2,
                "golsVisitante": 1
            }
        """.formatted(casa.getId(), visitante.getId(), estadio.getId());

        mockMvc.perform(post("/partida")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("Partida cadastrada")));
    }

    @Test
    void cadastrarPartidaClubeNaoExiste() throws Exception {
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        String json = """
            {
                "clubeCasaId": 999,
                "clubeVisitanteId": %d,
                "estadioId": %d,
                "dataHora": "2025-08-10T19:00:00",
                "golsCasa": 2,
                "golsVisitante": 1
            }
        """.formatted(visitante.getId(), estadio.getId());

        mockMvc.perform(post("/partida")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Clube da casa não existe!")));
    }

    @Test
    void cadastrarPartidaClubeInativo() throws Exception {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), false);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        String json = """
            {
                "clubeCasaId": %d,
                "clubeVisitanteId": %d,
                "estadioId": %d,
                "dataHora": "2025-08-10T19:00:00",
                "golsCasa": 2,
                "golsVisitante": 1
            }
        """.formatted(casa.getId(), visitante.getId(), estadio.getId());

        mockMvc.perform(post("/partida")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Clube da casa está inativo.")));
    }

    @Test
    void cadastrarPartidaDadosInvalidos() throws Exception {
        String json = """
            {
                "clubeCasaId": null,
                "clubeVisitanteId": null,
                "estadioId": null,
                "dataHora": null,
                "golsCasa": -1,
                "golsVisitante": null
            }
        """;

        mockMvc.perform(post("/partida")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastrarPartidaClubesIguais() throws Exception {
        Clube clube = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        String json = """
            {
                "clubeCasaId": %d,
                "clubeVisitanteId": %d,
                "estadioId": %d,
                "dataHora": "2025-08-10T19:00:00",
                "golsCasa": 2,
                "golsVisitante": 1
            }
        """.formatted(clube.getId(), clube.getId(), estadio.getId());

        mockMvc.perform(post("/partida")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Clubes não podem ser iguais!")));
    }

    @Test
    void atualizarPartidaComSucesso() throws Exception {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(
                casa, visitante, morumbi, LocalDateTime.of(2025, 8, 1, 20, 0), 2, 1);

        Estadio allianz = criarSalvarEstadio("Allianz Parque");

        String json = """
            {
                "clubeCasaId": %d,
                "clubeVisitanteId": %d,
                "estadioId": %d,
                "dataHora": "2025-08-15T21:00:00",
                "golsCasa": 3,
                "golsVisitante": 2
            }
        """.formatted(casa.getId(), visitante.getId(), allianz.getId());

        mockMvc.perform(put("/partida/" + partida.getId())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Partida atualizada com sucesso!")));
    }

    @Test
    void atualizarPartidaNaoEncontrada() throws Exception {
        String json = """
            {
                "clubeCasaId": 1,
                "clubeVisitanteId": 2,
                "estadioId": 1,
                "dataHora": "2025-08-10T19:00:00",
                "golsCasa": 2,
                "golsVisitante": 1
            }
        """;

        mockMvc.perform(put("/partida/9999")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Partida não encontrada!")));
    }

    @Test
    void atualizarPartidaClubesIguais() throws Exception {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(casa, casa, estadio, LocalDateTime.of(2025, 8, 1, 20, 0), 1, 1);

        String json = """
            {
                "clubeCasaId": %d,
                "clubeVisitanteId": %d,
                "estadioId": %d,
                "dataHora": "2025-08-15T21:00:00",
                "golsCasa": 2,
                "golsVisitante": 2
            }
        """.formatted(casa.getId(), casa.getId(), estadio.getId());

        mockMvc.perform(put("/partida/" + partida.getId())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Clubes não podem ser iguais!")));
    }

    @Test
    void atualizarPartidaGolsNegativos() throws Exception {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(
                casa, visitante, estadio, LocalDateTime.of(2025, 8, 1, 20, 0), 1, 1);

        String json = """
            {
                "clubeCasaId": %d,
                "clubeVisitanteId": %d,
                "estadioId": %d,
                "dataHora": "2025-08-15T21:00:00",
                "golsCasa": -1,
                "golsVisitante": 1
            }
        """.formatted(casa.getId(), visitante.getId(), estadio.getId());

        mockMvc.perform(put("/partida/" + partida.getId())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Gols não podem ser negativos!")));
    }

    @Test
    void atualizarPartidaClubeConflito48h() throws Exception {
        Clube casa = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube visitante = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        Partida original = criarSalvarPartida(
                casa, visitante, estadio, LocalDateTime.of(2025, 8, 1, 20, 0), 1, 1
        );
        criarSalvarPartida(casa, visitante, estadio, LocalDateTime.of(2025, 8, 3, 19, 0), 2, 1);

        String json = """
        {
            "clubeCasaId": %d,
            "clubeVisitanteId": %d,
            "estadioId": %d,
            "dataHora": "2025-08-03T19:00:00",
            "golsCasa": 1,
            "golsVisitante": 1
        }
    """.formatted(casa.getId(), visitante.getId(), estadio.getId());

        mockMvc.perform(put("/partida/" + original.getId())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Um dos clubes já tem partida marcada em menos de 48h")));
    }

    @Test
    void removerPartidaComSucesso() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025, 8, 1, 20, 0), 2, 1);

        mockMvc.perform(delete("/partida/" + partida.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Partida deletada!"));

        assertFalse(partidaRepository.findById(partida.getId()).isPresent());
    }

    @Test
    void removerPartidaNaoEncontrada() throws Exception {
        mockMvc.perform(delete("/partida/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Partida não encontrada!"));
    }

    @Test
    void buscarPartidaPorIdComSucesso() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        Partida partida = criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025, 8, 1, 20, 0), 2, 1);

        mockMvc.perform(get("/partida/" + partida.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(partida.getId()))
                .andExpect(jsonPath("$.clubeCasa.nome").value("Corinthians"))
                .andExpect(jsonPath("$.clubeVisitante.nome").value("Palmeiras"))
                .andExpect(jsonPath("$.estadio.nome").value("Morumbi"));
    }

    @Test
    void buscarPartidaPorIdNaoEncontrada() throws Exception {
        mockMvc.perform(get("/partida/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Partida não encontrada"));
    }

    @Test
    void listarPartidasTodasSemFiltro() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,8,1,19,0), 2, 1);
        criarSalvarPartida(c2, c1, estadio, LocalDateTime.of(2025,8,2,19,0), 1, 2);

        mockMvc.perform(get("/partida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void listarPartidasPorClube() throws Exception {
        Clube corinthians = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube palmeiras = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");
        criarSalvarPartida(corinthians, palmeiras, estadio, LocalDateTime.of(2025,8,1,19,0), 2, 1);

        mockMvc.perform(get("/partida?clubeId=" + corinthians.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].clubeCasa.nome").value("Corinthians"));
    }

    @Test
    void listarPartidasPorEstadio() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        Estadio allianz = criarSalvarEstadio("Allianz Parque");
        criarSalvarPartida(c1, c2, morumbi, LocalDateTime.of(2025,8,1,19,0), 1, 0);
        criarSalvarPartida(c2, c1, allianz, LocalDateTime.of(2025,8,2,19,0), 0, 1);

        mockMvc.perform(get("/partida?estadioId=" + morumbi.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].estadio.nome").value("Morumbi"));
    }

    @Test
    void listarPartidasGoleada() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,8,1,19,0), 6, 1); // goleada!
        criarSalvarPartida(c2, c1, estadio, LocalDateTime.of(2025,8,2,19,0), 2, 1);

        mockMvc.perform(get("/partida?goleada=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].golsCasa").value(6));
    }

    @Test
    void listarPartidasMandanteVisitante() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025,8,1,19,0), 3, 2); // c1 mandante
        criarSalvarPartida(c2, c1, estadio, LocalDateTime.of(2025,8,2,19,0), 2, 3); // c1 visitante

        mockMvc.perform(get("/partida?clubeId=" + c1.getId() + "&mandante=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].clubeCasa.nome").value("Corinthians"));

        mockMvc.perform(get("/partida?clubeId=" + c1.getId() + "&mandante=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].clubeVisitante.nome").value("Corinthians"));
    }

    @Test
    void listarPartidasSemResultado() throws Exception {
        mockMvc.perform(get("/partida?clubeId=99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void listarPartidasPaginacao() throws Exception {
        Clube c1 = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910,9,1), true);
        Clube c2 = criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914,8,26), true);
        Estadio estadio = criarSalvarEstadio("Morumbi");

        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025, 8, 1, 20, 0), 1, 1);
        criarSalvarPartida(c2, c1, estadio, LocalDateTime.of(2025, 8, 2, 20, 0), 2, 2);
        criarSalvarPartida(c1, c2, estadio, LocalDateTime.of(2025, 8, 3, 20, 0), 3, 0);

        mockMvc.perform(get("/partida?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
        mockMvc.perform(get("/partida?page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

}
