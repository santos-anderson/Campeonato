package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.EstadioRequestDTO;
import campeonato.com.Campeonato.exception.EstadioExisteException;
import campeonato.com.Campeonato.exception.EstadioNaoEncontradoException;
import campeonato.com.Campeonato.entity.Estadio;
import campeonato.com.Campeonato.repository.EstadioRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EstadioServiceTest {

    @Autowired
    EstadioRepository estadioRepository;

    @Autowired
    EstadioService estadioService;


    private Estadio criarSalvarEstadio(String nome) {
        Estadio estadio = new Estadio();
        estadio.setNome(nome);
        estadio.setCep("12345-123");

        return estadioRepository.save(estadio);
    }

    private EstadioRequestDTO criarEstadioRequestDTO(String nome) {
        EstadioRequestDTO estadioRequestDTO = new EstadioRequestDTO();
        estadioRequestDTO.setNome(nome);
        estadioRequestDTO.setCep("13054-411");

        return estadioRequestDTO;
    }

    @Test
    void cadastrarEstadio() {
        EstadioRequestDTO estadioRequestDTO = criarEstadioRequestDTO("Morumbis");

        String msg = estadioService.cadastrarEstadio(estadioRequestDTO);

        assertTrue(msg.contains("Morumbis"));
        Estadio estadio = estadioRepository.findByNomeIgnoreCase("Morumbis").orElse(null);
        assertNotNull(estadio);
        assertEquals("Morumbis", estadio.getNome());

    }

    @Test
    void cadastrarEstadioDuplicado() {
        EstadioRequestDTO estadioRequestDTO = criarEstadioRequestDTO("Morumbis");
        estadioService.cadastrarEstadio(estadioRequestDTO);

        EstadioExisteException ex = assertThrows(EstadioExisteException.class, () ->
                estadioService.cadastrarEstadio(estadioRequestDTO)
        );
        assertEquals("Já existe um Estádio com esse nome!.", ex.getMessage());
    }

    @Test
    void atualizarEstadio() {

        Estadio morumbis = criarSalvarEstadio("Morumbis");

        EstadioRequestDTO estadioRequestDTO = criarEstadioRequestDTO("Morumbis Morumbi");

        String msg = estadioService.atualizaEstadio(morumbis.getId(), estadioRequestDTO);

        Estadio atualizado = estadioRepository.findById(morumbis.getId()).orElse(null);
        assertNotNull(atualizado);
        assertEquals("Morumbis Morumbi", atualizado.getNome());
        assertTrue(msg.contains("Estádio atualizado com sucesso!"));
    }

    @Test
    void atualizarEstadioNaoEncontrado() {

        EstadioRequestDTO estadioRequestDTO = criarEstadioRequestDTO("Morumbis");
        EstadioNaoEncontradoException ex = assertThrows(EstadioNaoEncontradoException.class, () ->
                estadioService.atualizaEstadio(99L, estadioRequestDTO)
        );
        assertEquals("Estádio não encontrado!", ex.getMessage());
    }

    @Test
    void atualizarEstadioDuplicado() {

        Estadio morumbis = criarSalvarEstadio("Morumbis");

        criarSalvarEstadio("Maracanã");

        EstadioRequestDTO estadioRequestDTO = criarEstadioRequestDTO("Maracanã");

        EstadioExisteException ex = assertThrows(EstadioExisteException.class, () ->
                estadioService.atualizaEstadio(morumbis.getId(), estadioRequestDTO)
        );
        assertEquals("Já existe um estádio com esse nome!", ex.getMessage());
    }

    @Test
    void atualizarEstadioMesmoNome() {
        Estadio morumbi = criarSalvarEstadio("Morumbi");
        EstadioRequestDTO estadioRequestDTO = criarEstadioRequestDTO("Morumbi");

        String msg = estadioService.atualizaEstadio(morumbi.getId(), estadioRequestDTO);

        Estadio atualizado = estadioRepository.findById(morumbi.getId()).orElse(null);
        assertNotNull(atualizado);
        assertEquals("Morumbi", atualizado.getNome());
        assertTrue(msg.contains("Estádio atualizado com sucesso!"));
    }

    @Test
    void buscarEstadioPorId() {
        Estadio estadio = criarSalvarEstadio("Morumbis");
        Estadio resultado = estadioService.buscarEstadioPorId(estadio.getId());
        assertNotNull(resultado);
        assertEquals("Morumbis", resultado.getNome());
    }

    @Test
    void buscarEstadioPorIdNaoEncontrado() {
        EstadioNaoEncontradoException ex = assertThrows(EstadioNaoEncontradoException.class, () ->
                estadioService.buscarEstadioPorId(99L)
        );
        assertEquals("Estádio não encontrado!", ex.getMessage());
    }

    @Test
    void listarEstadioSemFiltro() {
        criarSalvarEstadio("Morumbi");
        criarSalvarEstadio("Allianz Parque");
        Page<Estadio> page = estadioService.listarEstadio(null, PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void listarEstadioFiltroNome() {
        criarSalvarEstadio("Morumbi");
        criarSalvarEstadio("Allianz Parque");
        Page<Estadio> page = estadioService.listarEstadio("mor", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Morumbi", page.getContent().get(0).getNome());

        page = estadioService.listarEstadio("allianz", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Allianz Parque", page.getContent().get(0).getNome());
    }

    @Test
    void listarEstadioVazioQuandoNaoEncontra() {
        criarSalvarEstadio("Morumbi");
        Page<Estadio> page = estadioService.listarEstadio("maracana", PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }
    @Test
    void listarEstadioPaginacao() {
        criarSalvarEstadio("Morumbi");
        criarSalvarEstadio("Allianz Parque");
        criarSalvarEstadio("Maracanã");

        Page<Estadio> page1 = estadioService.listarEstadio(null, PageRequest.of(0, 2));
        assertEquals(2, page1.getContent().size());

        Page<Estadio> page2 = estadioService.listarEstadio(null, PageRequest.of(1, 2));
        assertEquals(1, page2.getContent().size());

        assertEquals(3, page1.getTotalElements());
    }

}
