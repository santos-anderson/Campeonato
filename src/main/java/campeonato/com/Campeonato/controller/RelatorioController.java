package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.ClubeRankingDTO;
import campeonato.com.Campeonato.dto.RetrospectoClubeDTO;
import campeonato.com.Campeonato.dto.RetrospectoContraDTO;
import campeonato.com.Campeonato.entity.Partida;
import campeonato.com.Campeonato.services.RelatorioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/relatorios")
@Tag(name = "Relatórios", description = "Endpoints de análise e estatísticas dos clubes e partidas")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @Operation(summary = "Retrospecto geral de um clube", description = "Retorna vitórias, empates, derrotas, gols feitos e sofridos de um clube.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retrospecto retornado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Clube não encontrado.")
    })
    @GetMapping("/retrospecto/{clubeId}")
    public ResponseEntity<RetrospectoClubeDTO> retrospecto(@PathVariable Long clubeId) {
        RetrospectoClubeDTO retro = relatorioService.retrospectoClube(clubeId);
        return ResponseEntity.ok(retro);
    }

    @Operation(summary = "Retrospecto contra adversários", description = "Retorna o desempenho do clube contra cada adversário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de retrospectos retornada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Clube não encontrado.")
    })
    @GetMapping("/retrospecto-contra/{clubeId}")
    public ResponseEntity<List<RetrospectoContraDTO>> retrospectoContra(@PathVariable Long clubeId) {
        List<RetrospectoContraDTO> lista = relatorioService.retrospectoContraAdversarios(clubeId);
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Confronto direto entre clubes", description = "Retorna todas as partidas entre dois clubes, com resumo do confronto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Confronto retornado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Um dos clubes não foi encontrado.")
    })
    @GetMapping("/confrontos")
    public ResponseEntity<Map<String, Object>> confrontos(
            @RequestParam Long clubeA,
            @RequestParam Long clubeB
    ) {
        return ResponseEntity.ok(relatorioService.confrontosEntreClubes(clubeA, clubeB));
    }

    @Operation(summary = "Ranking de clubes", description = "Retorna ranking dos clubes com base em um critério: pontos, gols, vitórias ou jogos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking retornado com sucesso.")
    })
    @GetMapping("/ranking")
    public ResponseEntity<List<ClubeRankingDTO>> ranking(
            @RequestParam(name = "criterio", defaultValue = "pontos") String criterio
    ) {
        List<ClubeRankingDTO> ranking = relatorioService.ranking(criterio);
        return ResponseEntity.ok(ranking);
    }

    @Operation(summary = "Lista partidas com goleadas", description = "Lista partidas com diferença de 3 ou mais gols.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de partidas retornada com sucesso.")
    })
    @GetMapping("/goleadas")
    public Page<Partida> goleadas(
            @RequestParam(required = false) Long clubeId,
            @RequestParam(required = false) Long estadioId,
            @RequestParam(required = false) Boolean mandante,
            @PageableDefault(size = 10, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return relatorioService.partidasGoleada(clubeId, estadioId, mandante, pageable);
    }

    @Operation(summary = "Partidas de um clube como mandante ou visitante", description = "Filtra partidas de um clube com filtros adicionais.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de partidas retornada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Clube não encontrado.")
    })
    @GetMapping("/partidas-clube")
    public Page<Partida> partidasPorClube(
            @RequestParam Long clubeId,
            @RequestParam(required = false) Long estadioId,
            @RequestParam(required = false) Boolean goleada,
            @RequestParam(required = false) Boolean mandante,
            @PageableDefault(size = 10, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return relatorioService.partidasPorClube(clubeId, estadioId, goleada, mandante, pageable);
    }
}


