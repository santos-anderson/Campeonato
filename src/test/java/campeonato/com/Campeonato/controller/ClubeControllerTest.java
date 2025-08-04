package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.entity.Clube;
import campeonato.com.Campeonato.repository.ClubeRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ClubeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClubeRepository clubeRepository;

    private Clube criarSalvarClube(String nome, String uf, LocalDate dataCriacao, boolean status) {
        Clube clube = new Clube();
        clube.setNome(nome);
        clube.setUf(uf);
        clube.setDataCriacao(dataCriacao);
        clube.setStatus(status);
        return clubeRepository.save(clube);
    }

    @Test
    void cadastrarClubeComSucesso() throws Exception {
        String json = """
            {
                "nome": "Corinthians",
                "uf": "SP",
                "dataCriacao": "1910-09-01",
                "status": true
            }
        """;

        mockMvc.perform(post("/clube")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Corinthians")));
    }

        @Test
        void cadastrarClubeDuplicado() throws Exception {

            criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);

            String json = """
            {
                "nome": "Corinthians",
                "uf": "SP",
                "dataCriacao": "1910-09-01",
                "status": true
            }
        """;

            mockMvc.perform(post("/clube")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Já existe um clube com esse nome nesse estado.")));
        }

    @Test
    void cadastrarClubeComCampoInvalido() throws Exception {
        String json = """
            {
                "nome": "",
                "uf": "SP",
                "dataCriacao": "1910-09-01",
                "status": true
            }
        """;

        mockMvc.perform(post("/clube")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarClubeComSucesso() throws Exception {
        Clube clube = criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        String json = """
            {
                "nome": "Corinthians Paulista",
                "uf": "SP",
                "dataCriacao": "1910-09-02",
                "status": false
            }
        """;

        mockMvc.perform(put("/clube/" + clube.getId())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Clube atualizado com sucesso")));
    }

    @Test
    void atualizarClubeDuplicado() throws Exception {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        Clube clube2 = criarSalvarClube("Palmeiras", "SP",
                LocalDate.of(1914,8,26), true);

        String json = """
            {
                "nome": "Corinthians",
                "uf": "SP",
                "dataCriacao": "1910-09-01",
                "status": true
            }
        """;

        mockMvc.perform(put("/clube/" + clube2.getId())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Já existe um clube")));
    }

    @Test
    void atualizarClubeNaoencontrado() throws Exception {
        String json = """
            {
                "nome": "Corinthians",
                "uf": "SP",
                "dataCriacao": "1910-09-01",
                "status": true
            }
        """;

        mockMvc.perform(put("/clube/99")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Clube não encontrado")));
    }


    @Test
    void inativarClubeNaoEncontrado() throws Exception {
        mockMvc.perform(delete("/clube/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Clube não encontrado")));
    }

    @Test
    void buscarClubePorIdComSucesso() throws Exception {
        Clube clube = criarSalvarClube("Corinthians", "SP",
                LocalDate.of(1910, 9, 1), true);

        mockMvc.perform(get("/clube/" + clube.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Corinthians"))
                .andExpect(jsonPath("$.uf").value("SP"));

    }

    @Test
    void buscarClubePorIdNaoEncontrado() throws Exception {
        mockMvc.perform(get("/clube/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Clube não encontrado")));
    }

    @Test
    void listarClubesSemFiltro() throws Exception {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), false);

        mockMvc.perform(get("/clube"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void listarClubesFiltroNome() throws Exception {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), true);

        mockMvc.perform(get("/clube?nome=corinth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Corinthians"))
                .andExpect(jsonPath("$.content.length()").value(1));
        mockMvc.perform(get("/clube?nome=palmeiras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Palmeiras"))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void listarClubesFiltroUf() throws Exception {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Flamengo", "RJ", LocalDate.of(1931, 1, 1), true);

        mockMvc.perform(get("/clube?uf=SP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Corinthians"))
                .andExpect(jsonPath("$.content.length()").value(1));
        mockMvc.perform(get("/clube?uf=RJ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Flamengo"))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void listarClubesFiltroStatus() throws Exception {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), false);

        mockMvc.perform(get("/clube?status=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Corinthians"))
                .andExpect(jsonPath("$.content.length()").value(1));
        mockMvc.perform(get("/clube?status=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Palmeiras"))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void listarClubesFiltroTodosNenhumEncontra() throws Exception {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), false);

        mockMvc.perform(get("/clube?nome=Bahia&uf=BA&status=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void listarClubesPaginacao() throws Exception {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), false);


        mockMvc.perform(get("/clube?page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/clube?page=1&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}




