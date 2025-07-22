package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import campeonato.com.Campeonato.exception.PartidaCadastroException;
import campeonato.com.Campeonato.exception.PartidaValidacaoException;
import campeonato.com.Campeonato.entity.Partida;
import campeonato.com.Campeonato.services.PartidaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/partida")
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    @Operation(summary = "Cadastra Partida", description = "Cadastra a partida e valida os dados informado!.")
    @PostMapping
    public ResponseEntity<String> cadastrarPartida(@RequestBody @Valid PartidaRequestDTO dto) {
        try {
            String msg = partidaService.cadastrarPartida(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(msg);
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (PartidaCadastroException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    @Operation(summary = "Atualiza Partida", description = "Atualiza a partida pelo ID.")
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

    @Operation(summary = "Deleta Partida", description = "Deleta a partida pelo ID informado.")
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

    @Operation(summary = "Busca partida", description = "Busca a partida pelo ID informado.")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPartida(@PathVariable Long id) {
        try {
            Partida partida = partidaService.buscarPartida(id);
            return ResponseEntity.ok(partida);
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Partida não encontrada");
        }
    }

    @Operation(summary = "Lista Partidas", description = "Lista Partidas.")
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
}