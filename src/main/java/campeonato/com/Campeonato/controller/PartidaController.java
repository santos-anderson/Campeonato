package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import campeonato.com.Campeonato.entity.Partida;
import campeonato.com.Campeonato.services.PartidaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

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
@Tag(name = "Partidas", description = "Operações relacionadas às partidas de futebol")
public class PartidaController {

    @Autowired
    private PartidaService partidaService;

    @Operation(summary = "Cadastrar partida", description = "Cadastra uma nova partida com validação completa dos dados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Partida cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (clubes iguais, gols negativos, etc.)"),
            @ApiResponse(responseCode = "409", description = "Conflito com dados existentes (clube inativo, horário inválido, etc.)")
    })
    @PostMapping
    public ResponseEntity<String> cadastrarPartida(
            @RequestBody @Valid PartidaRequestDTO dto) {
        String msg = partidaService.cadastrarPartida(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(msg);
    }

    @Operation(summary = "Enviar partida para fila", description = "Envia a partida para processamento assíncrono via RabbitMQ.")
    @ApiResponse(responseCode = "202", description = "Partida enviada para fila com sucesso")
    @PostMapping("/fila")
    public ResponseEntity<String> cadastrarPartidaFila(
            @RequestBody @Valid PartidaRequestDTO dto) {
        String msg = partidaService.cadastrarPartidaFila(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(msg);
    }

    @Operation(summary = "Atualizar partida", description = "Atualiza os dados de uma partida existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partida atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Partida não encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflito com horário, estádio, clube inativo, etc.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarPartida(
            @Parameter(description = "ID da partida") @PathVariable Long id,
            @RequestBody @Valid PartidaRequestDTO dto) {
        String msg = partidaService.atualizarPartida(id, dto);
        return ResponseEntity.ok(msg);
    }

    @Operation(summary = "Remover partida", description = "Remove uma partida pelo ID informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Partida removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Partida não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerPartida(
            @Parameter(description = "ID da partida") @PathVariable Long id) {
        partidaService.removerPartida(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar partida", description = "Busca uma partida pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partida encontrada"),
            @ApiResponse(responseCode = "404", description = "Partida não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Partida> buscarPartida(
            @Parameter(description = "ID da partida") @PathVariable Long id) {
        Partida partida = partidaService.buscarPartida(id);
        return ResponseEntity.ok(partida);
    }

    @Operation(summary = "Listar partidas", description = "Lista partidas com filtros opcionais (clube, estádio, goleada, mandante) e paginação.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso (mesmo se vazia)")
    @GetMapping
    public Page<Partida> listarPartidas(
            @Parameter(description = "ID do clube para filtrar") @RequestParam(required = false) Long clubeId,
            @Parameter(description = "ID do estádio para filtrar") @RequestParam(required = false) Long estadioId,
            @Parameter(description = "Filtrar partidas que foram goleadas") @RequestParam(required = false) Boolean goleada,
            @Parameter(description = "Filtrar partidas como mandante") @RequestParam(required = false) Boolean mandante,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return partidaService.listarPartidas(clubeId, estadioId, goleada, mandante, pageable);
    }
}

