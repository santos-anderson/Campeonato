package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import campeonato.com.Campeonato.entity.Partida;
import campeonato.com.Campeonato.exception.PartidaCadastroException;
import campeonato.com.Campeonato.exception.PartidaValidacaoException;
import campeonato.com.Campeonato.services.PartidaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/partida")
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    @Operation(summary = "Cadastra Partida", description = "Cadastra a partida com validação imediata.")
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

    @Operation(summary = "Envia Partida para Fila", description = "Envia a partida para processamento assíncrono via fila RabbitMQ.")
    @PostMapping("/fila")
    public ResponseEntity<String> cadastrarPartidaFila(@RequestBody PartidaRequestDTO dto) {
        try {
            String msg = partidaService.cadastrarPartidaFila(dto);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(msg);
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (PartidaCadastroException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    @Operation(summary = "Atualiza Partida", description = "Atualiza os dados de uma partida existente.")
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

    @Operation(summary = "Deleta Partida", description = "Remove a partida com base no ID informado.")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> removerPartida(@PathVariable Long id) {
        try {
            partidaService.removerPartida(id);
            return ResponseEntity.ok("Partida deletada!");
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Partida não encontrada!");
        }
    }

    @Operation(summary = "Busca Partida", description = "Busca uma partida específica pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPartida(@PathVariable Long id) {
        try {
            Partida partida = partidaService.buscarPartida(id);
            return ResponseEntity.ok(partida);
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Partida não encontrada");
        }
    }

    @Operation(summary = "Lista Partidas", description = "Lista partidas com filtros opcionais.")
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
