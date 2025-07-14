package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.ClubeRequestDTO;
import campeonato.com.Campeonato.exception.ClubeExisteException;
import campeonato.com.Campeonato.model.Clube;
import campeonato.com.Campeonato.repository.ClubeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
public class ClubeServicesTest {

    @Autowired
    private ClubeRepository clubeRepository;
    @Autowired
    private ClubeService clubeService;

    @Test
    void CadastrarClube() {
        ClubeRequestDTO dto = new ClubeRequestDTO();
        dto.setNome("Corinthians");
        dto.setUf("SP");
        dto.setDataCriacao(LocalDate.of(1910, 9, 1));
        dto.setStatus(true);

        String msg = clubeService.cadastrarClube(dto);

        assertTrue(msg.contains("Corinthians"));
        Clube clube = clubeRepository.findByNomeAndUfIgnoreCase("Corinthians", "SP").orElse(null);
        assertNotNull(clube);
        assertEquals("Corinthians", clube.getNome());
        assertEquals("SP", clube.getUf());
    }

}
