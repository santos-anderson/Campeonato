package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.ClubeRequestDTO;
import campeonato.com.Campeonato.exception.ClubeExisteException;
import campeonato.com.Campeonato.exception.ClubeNaoEncontradoException;
import campeonato.com.Campeonato.model.Clube;
import campeonato.com.Campeonato.repository.ClubeRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ClubeServicesTest {

    @Autowired
    private ClubeRepository clubeRepository;
    @Autowired
    private ClubeService clubeService;

    @Test
    void cadastrarClube() {
        ClubeRequestDTO clubeRequestDTO = new ClubeRequestDTO();
        clubeRequestDTO.setNome("Corinthians");
        clubeRequestDTO.setUf("SP");
        clubeRequestDTO.setDataCriacao(LocalDate.of(1910, 9, 1));
        clubeRequestDTO.setStatus(true);

        String msg = clubeService.cadastrarClube(clubeRequestDTO);

        assertTrue(msg.contains("Corinthians"));
        Clube clube = clubeRepository.findByNomeAndUfIgnoreCase("Corinthians", "SP").orElse(null);
        assertNotNull(clube);
        assertEquals("Corinthians", clube.getNome());
        assertEquals("SP", clube.getUf());
    }

    @Test
    void cadastrarClubeDuplicado() {
        ClubeRequestDTO clubeRequestDTO = new ClubeRequestDTO();
        clubeRequestDTO.setNome("Corinthians");
        clubeRequestDTO.setUf("SP");
        clubeRequestDTO.setDataCriacao(LocalDate.of(1910, 9, 1));
        clubeRequestDTO.setStatus(true);

        clubeService.cadastrarClube(clubeRequestDTO);


        Exception ex = assertThrows(ClubeExisteException.class, () ->
                clubeService.cadastrarClube(clubeRequestDTO)
        );
        assertTrue(ex.getMessage().contains("Já existe um clube com esse nome nesse estado."));
    }

    @Test
    void atualizarClube() {

        Clube clube = new Clube();
        clube.setNome("Corinthians");
        clube.setUf("SP");
        clube.setDataCriacao(LocalDate.of(1910, 9, 1));
        clube.setStatus(true);
        clube = clubeRepository.save(clube);

        ClubeRequestDTO clubeRequestDTO = new ClubeRequestDTO();
        clubeRequestDTO.setNome("Corinthians Paulista");
        clubeRequestDTO.setUf("SP");
        clubeRequestDTO.setDataCriacao(LocalDate.of(1910, 9, 2));
        clubeRequestDTO.setStatus(false);

        String msg = clubeService.atualizarClube(clube.getId(), clubeRequestDTO);

        Clube atualizado = clubeRepository.findById(clube.getId()).orElse(null);
        assertNotNull(atualizado);
        assertEquals("Corinthians Paulista", atualizado.getNome());
        assertEquals(LocalDate.of(1910, 9, 2), atualizado.getDataCriacao());
        assertFalse(atualizado.getStatus());
        assertTrue(msg.contains("atualizado com sucesso"));
    }

    @Test
    void atualizarClubeNaoEncontrado() {

        ClubeRequestDTO clubeRequestDTO = new ClubeRequestDTO();
        clubeRequestDTO.setNome("Corinthians");
        clubeRequestDTO.setUf("SP");
        clubeRequestDTO.setDataCriacao(LocalDate.of(1910, 9, 1));
        clubeRequestDTO.setStatus(true);

        Exception ex = assertThrows(ClubeNaoEncontradoException.class, () ->
                clubeService.atualizarClube(99L, clubeRequestDTO)
        );
        assertTrue(ex.getMessage().contains("Clube não encontrado"));
    }

    @Test
    void atualizarClubeDuplicado() {

        Clube corinthians = new Clube();
        corinthians.setNome("Corinthians");
        corinthians.setUf("SP");
        corinthians.setDataCriacao(LocalDate.of(1910, 9, 1));
        corinthians.setStatus(true);
        corinthians = clubeRepository.save(corinthians);

        Clube palmeiras = new Clube();
        palmeiras.setNome("Palmeiras");
        palmeiras.setUf("SP");
        palmeiras.setDataCriacao(LocalDate.of(1914, 8, 26));
        palmeiras.setStatus(true);
        clubeRepository.save(palmeiras);

        Long corinthiansId = corinthians.getId();

        ClubeRequestDTO clubeRequestDTO = new ClubeRequestDTO();
        clubeRequestDTO.setNome("Palmeiras");
        clubeRequestDTO.setUf("SP");
        clubeRequestDTO.setDataCriacao(LocalDate.of(1914, 8, 26));
        clubeRequestDTO.setStatus(true);

        ClubeExisteException ex = assertThrows(ClubeExisteException.class, () ->
                clubeService.atualizarClube(corinthiansId, clubeRequestDTO)
        );

        assertEquals("Já existe um clube chamado 'Palmeiras' no estado 'SP'.", ex.getMessage());
    }

}
