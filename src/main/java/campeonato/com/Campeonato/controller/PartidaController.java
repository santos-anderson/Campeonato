package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.ClubeRankingDTO;
import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import campeonato.com.Campeonato.dto.RetrospectoClubeDTO;
import campeonato.com.Campeonato.dto.RetrospectoContraDTO;
import campeonato.com.Campeonato.exception.PartidaCadastroException;
import campeonato.com.Campeonato.exception.PartidaValidacaoException;
import campeonato.com.Campeonato.model.Partida;
import campeonato.com.Campeonato.services.PartidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/partida")
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    @PostMapping
    public ResponseEntity<String> cadastrarPartida(@RequestBody PartidaRequestDTO dto) {
        try {
            String msg = partidaService.cadastrarPartida(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(msg);
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (PartidaCadastroException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarPartida(@PathVariable Long id, @RequestBody PartidaRequestDTO dto) {
        try {
            String msg = partidaService.atualizarPartida(id, dto);
            return ResponseEntity.ok(msg);
        } catch (PartidaValidacaoException ex) {
            if ("Partida não encontrada!".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
            }
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (PartidaCadastroException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> removerPartida(@PathVariable Long id) {
        try {
            partidaService.removerPartida(id);
            return ResponseEntity.ok("Partida deletada!");
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Partida não encontrada!");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPartida(@PathVariable Long id) {
        try {
            Partida partida = partidaService.buscarPartida(id);
            return ResponseEntity.ok(partida);
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Partida não encontrada");
        }
    }

    @GetMapping
    public Page<Partida> listarPartidas(
            @RequestParam(required = false) Long clubeId,
            @RequestParam(required = false) Long estadioId,
            @RequestParam(required = false) Boolean goleada,
            @RequestParam(required = false) Boolean mandante,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return partidaService.listarPartidas(clubeId, estadioId, goleada, mandante, pageable);
    }

    @GetMapping("/retrospecto/{clubeId}")
    public ResponseEntity<RetrospectoClubeDTO> retrospecto(@PathVariable Long clubeId) {
        try {
            RetrospectoClubeDTO retro = partidaService.retrospectoClube(clubeId);
            return ResponseEntity.ok(retro);
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/retrospecto-contra/{clubeId}")
    public ResponseEntity<List<RetrospectoContraDTO>> retrospectoContra(@PathVariable Long clubeId) {
        try {
            List<RetrospectoContraDTO> lista = partidaService.retrospectoContraAdversarios(clubeId);
            return ResponseEntity.ok(lista);
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/confrontos")
    public ResponseEntity<Map<String, Object>> confrontos(
            @RequestParam Long clubeA, @RequestParam Long clubeB) {
        return ResponseEntity.ok(partidaService.confrontosEntreClubes(clubeA, clubeB));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ClubeRankingDTO>> ranking(
            @RequestParam(name = "criterio", defaultValue = "pontos") String criterio) {
        List<ClubeRankingDTO> ranking = partidaService.ranking(criterio);
        return ResponseEntity.ok(ranking);
    }
}