package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import campeonato.com.Campeonato.services.PartidaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PartidaControllerFilaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartidaService partidaService;

    @Test
    void deveEnviarPartidaParaFilaComSucesso() throws Exception {

        Mockito.when(partidaService.cadastrarPartidaFila(any(PartidaRequestDTO.class)))
                .thenReturn("Partida enviada para fila com sucesso");

        String json = """
            {
                "clubeCasaId": 1,
                "clubeVisitanteId": 2,
                "estadioId": 3,
                "dataHora": "2025-08-15T19:00:00",
                "golsCasa": 2,
                "golsVisitante": 1
            }
        """;

        mockMvc.perform(post("/partida/fila")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Partida enviada para fila com sucesso"));
    }
}
