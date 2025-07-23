package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.entity.Estadio;
import campeonato.com.Campeonato.repository.EstadioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EstadioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EstadioRepository estadioRepository;

    private Estadio criarSalvarEstadio(String nome) {
        Estadio estadio = new Estadio();
        estadio.setNome(nome);
        estadio.setCep("13054-411");
        return estadioRepository.save(estadio);
    }

    @Test
    void cadastrarEstadioComSucesso() throws Exception {
        String json = """
            {
                "nome": "Morumbi",
                "cep":"13054-411" 
            }
        """;

        mockMvc.perform(post("/estadio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("Morumbi")));
    }

    @Test
    void cadastrarEstadioDuplicado() throws Exception {
        criarSalvarEstadio("Morumbi");

        String json = """
            {
                "nome": "Morumbi",
                "cep":"13054-411"
            }
        """;

        mockMvc.perform(post("/estadio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Já existe um Estádio com esse nome!.")));
    }

    @Test
    void cadastrarEstadioComNomeInvalido() throws Exception {
        String json = """
            {
                "nome": "Mo"
            }
        """;

        mockMvc.perform(post("/estadio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarEstadioComSucesso() throws Exception {
        Estadio estadio = criarSalvarEstadio("Morumbi");

        String json = """
            {
                "nome": "Morumbis",
                "cep":"13054-411"
            }
        """;

        mockMvc.perform(put("/estadio/" + estadio.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Estádio atualizado com sucesso!")));
    }

    @Test
    void atualizarEstadioDuplicado() throws Exception {
        Estadio estadio1 = criarSalvarEstadio("Morumbi");
        criarSalvarEstadio("Maracanã");

        String json = """
            {
                "nome": "Maracanã",
                "cep":"13054-411"
            }
        """;

        mockMvc.perform(put("/estadio/" + estadio1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Já existe um estádio com esse nome!")));
    }

    @Test
    void atualizarEstadioNaoExiste() throws Exception {
        String json = """
            {
                "nome": "Moises Lucarelli",
                "cep":"13054-411"
            }
        """;

        mockMvc.perform(put("/estadio/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Estádio não encontrado")));
    }

    @Test
    void atualizarEstadioNomeInvalido() throws Exception {
        Estadio estadio = criarSalvarEstadio("Morumbi");

        String json = """
            {
                "nome": "Mo"
            }
        """;

        mockMvc.perform(put("/estadio/" + estadio.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizarEstadioMesmoNome() throws Exception {
        Estadio estadio = criarSalvarEstadio("Morumbi");

        String json = """
            {
                "nome": "Morumbi",
                "cep":"13054-411"
            }
        """;

        mockMvc.perform(put("/estadio/" + estadio.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Estádio atualizado com sucesso!")));
    }

    @Test
    void buscarEstadioPorIdComSucesso() throws Exception {
        Estadio estadio = criarSalvarEstadio("Morumbi");

        mockMvc.perform(get("/estadio/" + estadio.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Morumbi"));
    }

    @Test
    void buscarEstadioPorIdNaoExiste() throws Exception {
        mockMvc.perform(get("/estadio/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Estádio não encontrado")));
    }

    @Test
    void listarEstadioSemFiltro() throws Exception {
        criarSalvarEstadio("Morumbi");
        criarSalvarEstadio("Allianz Parque");

        mockMvc.perform(get("/estadio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void listarEstadioFiltroNome() throws Exception {
        criarSalvarEstadio("Morumbi");
        criarSalvarEstadio("Allianz Parque");

        mockMvc.perform(get("/estadio?nome=mor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Morumbi"));

        mockMvc.perform(get("/estadio?nome=allianz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Allianz Parque"));
    }

    @Test
    void listarEstadioListaNada() throws Exception {
        criarSalvarEstadio("Morumbi");

        mockMvc.perform(get("/estadio?nome=maracana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void listarEstadioPaginacao() throws Exception {
        criarSalvarEstadio("Morumbi");
        criarSalvarEstadio("Allianz Parque");
        criarSalvarEstadio("Maracanã");

        mockMvc.perform(get("/estadio?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get("/estadio?page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
