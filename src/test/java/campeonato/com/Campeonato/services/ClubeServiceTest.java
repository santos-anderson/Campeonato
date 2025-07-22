package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.ClubeRequestDTO;
import campeonato.com.Campeonato.exception.ClubeExisteException;
import campeonato.com.Campeonato.exception.ClubeNaoEncontradoException;
import campeonato.com.Campeonato.entity.Clube;
import campeonato.com.Campeonato.repository.ClubeRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ClubeServiceTest {

    @Autowired
    private ClubeRepository clubeRepository;

    @Autowired
    private ClubeService clubeService;


    private Clube criarSalvarClube(String nome, String uf, LocalDate dataCriacao, boolean status) {
        Clube clube = new Clube();
        clube.setNome(nome);
        clube.setUf(uf);
        clube.setDataCriacao(dataCriacao);
        clube.setStatus(status);
        return clubeRepository.save(clube);
    }

    private ClubeRequestDTO criarClubeRequestDTO(String nome, String uf, LocalDate dataCriacao, boolean status) {
        ClubeRequestDTO clubeRequestDTO = new ClubeRequestDTO();
        clubeRequestDTO.setNome(nome);
        clubeRequestDTO.setUf(uf);
        clubeRequestDTO.setDataCriacao(dataCriacao);
        clubeRequestDTO.setStatus(status);
        return clubeRequestDTO;
    }

    @Test
    void cadastrarClube() {
        ClubeRequestDTO clubeRequestDTO = criarClubeRequestDTO("Corinthians", "SP",
                LocalDate.of(1910, 9, 1), true);

        String msg = clubeService.cadastrarClube(clubeRequestDTO);

        assertTrue(msg.contains("Corinthians"));
        Clube clube = clubeRepository.findByNomeAndUfIgnoreCase("Corinthians", "SP").orElse(null);
        assertNotNull(clube);
        assertEquals("Corinthians", clube.getNome());
        assertEquals("SP", clube.getUf());
    }

    @Test
    void cadastrarClubeDuplicado() {
        ClubeRequestDTO clubeRequestDTO = criarClubeRequestDTO("Corinthians", "SP",
                LocalDate.of(1910, 9, 1), true);
        clubeService.cadastrarClube(clubeRequestDTO);

        ClubeExisteException ex = assertThrows(ClubeExisteException.class, () ->
                clubeService.cadastrarClube(clubeRequestDTO)
        );
        assertEquals("Já existe um clube com esse nome nesse estado.", ex.getMessage());
    }

    @Test
    void atualizarClube() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP",
                LocalDate.of(1910, 9, 1), true);
        ClubeRequestDTO clubeRequestDTO = criarClubeRequestDTO("Corinthians Paulista", "SP",
                LocalDate.of(1910, 9, 2), false);

        String msg = clubeService.atualizarClube(corinthians.getId(), clubeRequestDTO);

        Clube atualizado = clubeRepository.findById(corinthians.getId()).orElse(null);
        assertNotNull(atualizado);
        assertEquals("Corinthians Paulista", atualizado.getNome());
        assertEquals(LocalDate.of(1910, 9, 2), atualizado.getDataCriacao());
        assertFalse(atualizado.getStatus());
        assertTrue(msg.contains("atualizado com sucesso"));
    }

    @Test
    void atualizarClubeNaoEncontrado() {
        ClubeRequestDTO clubeRequestDTO = criarClubeRequestDTO("Corinthians", "SP",
                LocalDate.of(1910, 9, 1), true);
        ClubeNaoEncontradoException ex = assertThrows(ClubeNaoEncontradoException.class, () ->
                clubeService.atualizarClube(99L, clubeRequestDTO)
        );
        assertEquals("Clube não encontrado!", ex.getMessage());
    }

    @Test
    void atualizarClubeDuplicado() {
        Clube corinthians = criarSalvarClube("Corinthians", "SP",
                LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Palmeiras", "SP",
                LocalDate.of(1914, 8, 26), true);

        ClubeRequestDTO clubeRequestDTO = criarClubeRequestDTO("Palmeiras", "SP",
                LocalDate.of(1914, 8, 26), true);

        ClubeExisteException ex = assertThrows(ClubeExisteException.class, () ->
                clubeService.atualizarClube(corinthians.getId(), clubeRequestDTO)
        );
        assertEquals("Já existe um clube chamado 'Palmeiras' no estado 'SP'.", ex.getMessage());
    }

    @Test
    void inativarClubeAtivo() {
        Clube clube = criarSalvarClube("Corinthians", "SP",
                LocalDate.of(1910, 9, 1), true);
        clubeService.inativarClube(clube.getId());
        Clube atualizado = clubeRepository.findById(clube.getId()).orElse(null);
        assertNotNull(atualizado);
        assertFalse(atualizado.getStatus());
    }

    @Test
    void inativarClubeInativo() {
        Clube clube = criarSalvarClube("Palmeiras", "SP",
                LocalDate.of(1914, 8, 26), false);
        clubeService.inativarClube(clube.getId());
        Clube atualizado = clubeRepository.findById(clube.getId()).orElse(null);
        assertNotNull(atualizado);
        assertFalse(atualizado.getStatus());
    }

    @Test
    void inativarClubeNaoEncontrado() {
        ClubeNaoEncontradoException ex = assertThrows(ClubeNaoEncontradoException.class, () ->
                clubeService.inativarClube(99L)
        );
        assertEquals("Clube não encontrado!", ex.getMessage());
    }

    @Test
    void buscarClubePorId() {
        Clube clube = criarSalvarClube("Corinthians", "SP",
                LocalDate.of(1910, 9, 1), true);
        Clube resultado = clubeService.buscarClubePorId(clube.getId());
        assertNotNull(resultado);
        assertEquals("Corinthians", resultado.getNome());
        assertEquals("SP", resultado.getUf());
    }

    @Test
    void buscarClubePorIdNaoEncontrado() {
        ClubeNaoEncontradoException ex = assertThrows(ClubeNaoEncontradoException.class, () ->
                clubeService.buscarClubePorId(99L)
        );
        assertEquals("Clube não encontrado!", ex.getMessage());
    }

    @Test
    void listarClubesFiltroNome() {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), false);

        Page<Clube> page = clubeService.listarClubes("corinth", null, null, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Corinthians", page.getContent().get(0).getNome());

        page = clubeService.listarClubes("palmeiras", null, null, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Palmeiras", page.getContent().get(0).getNome());
    }

    @Test
    void listarClubesFiltroUF() {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Flamengo", "RJ", LocalDate.of(1914, 8, 26), false);

        Page<Clube> pageSP = clubeService.listarClubes(null, "SP", null, PageRequest.of(0, 10));
        assertEquals(1, pageSP.getTotalElements());
        assertEquals("Corinthians", pageSP.getContent().get(0).getNome());

        Page<Clube> pageRJ = clubeService.listarClubes(null, "RJ", null, PageRequest.of(0, 10));
        assertEquals(1, pageRJ.getTotalElements());
        assertEquals("Flamengo", pageRJ.getContent().get(0).getNome());
    }

    @Test
    void listarClubesFiltroStatus() {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), false);

        Page<Clube> ativos = clubeService.listarClubes(null, null, true,
                PageRequest.of(0, 10));
        assertEquals(1, ativos.getTotalElements());
        assertEquals("Corinthians", ativos.getContent().get(0).getNome());

        Page<Clube> inativos = clubeService.listarClubes(null, null, false,
                PageRequest.of(0, 10));
        assertEquals(1, inativos.getTotalElements());
        assertEquals("Palmeiras", inativos.getContent().get(0).getNome());
    }

    @Test
    void listarClubesTodosFiltros() {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), false);


        Page<Clube> page = clubeService.listarClubes("Bahia", "RJ", true,
                PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void listarClubesPaginacao() {
        criarSalvarClube("Corinthians", "SP", LocalDate.of(1910, 9, 1), true);
        criarSalvarClube("Palmeiras", "SP", LocalDate.of(1914, 8, 26), false);

        Page<Clube> page1 = clubeService.listarClubes(null, null, null,
                PageRequest.of(0, 1));
        assertEquals(1, page1.getContent().size());

        Page<Clube> page2 = clubeService.listarClubes(null, null, null,
                PageRequest.of(1, 1));
        assertEquals(1, page2.getContent().size());
        assertEquals(2, page1.getTotalElements());
    }
}
