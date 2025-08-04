package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.ClubeRankingDTO;
import campeonato.com.Campeonato.dto.RetrospectoClubeDTO;
import campeonato.com.Campeonato.dto.RetrospectoContraDTO;
import campeonato.com.Campeonato.entity.Partida;
import campeonato.com.Campeonato.services.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }
    @Operation(summary = "Restropecto de um clube", description = "Retrospecto de um clube pelo ID informado!.")
    @GetMapping("/retrospecto/{clubeId}")
    public ResponseEntity<RetrospectoClubeDTO> retrospecto(@PathVariable Long clubeId) {
        RetrospectoClubeDTO retro = relatorioService.retrospectoClube(clubeId);
        return ResponseEntity.ok(retro);
    }

    @Operation(summary = "Retrospecto de um clube contra os advesarios", description = "Retrospecto de um clube contra seus adversarios pelo ID.")
    @GetMapping("/retrospecto-contra/{clubeId}")
    public ResponseEntity<List<RetrospectoContraDTO>> retrospectoContra(@PathVariable Long clubeId) {
        List<RetrospectoContraDTO> lista = relatorioService.retrospectoContraAdversarios(clubeId);
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Confrontos entre clubes", description = "Confronto entre os clubes informados!.")
    @GetMapping("/confrontos")
    public ResponseEntity<Map<String, Object>> confrontos(
            @RequestParam Long clubeA, @RequestParam Long clubeB) {
        return ResponseEntity.ok(relatorioService.confrontosEntreClubes(clubeA, clubeB));
    }

    @Operation(summary = "Ranking de clubes", description = "Ranking dos clubes por Pontos.")
    @GetMapping("/ranking")
    public ResponseEntity<List<ClubeRankingDTO>> ranking(
            @RequestParam(name = "criterio", defaultValue = "pontos") String criterio) {
        List<ClubeRankingDTO> ranking = relatorioService.ranking(criterio);
        return ResponseEntity.ok(ranking);
    }

    @Operation(summary = "Goleadas", description = "Partidas que foram goleadas entre clubes.")
    @GetMapping("/goleadas")
    public Page<Partida> goleadas(
            @RequestParam(required = false) Long clubeId,
            @RequestParam(required = false) Long estadioId,
            @RequestParam(required = false) Boolean mandante,
            @PageableDefault(size = 10, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return relatorioService.partidasGoleada(clubeId, estadioId, mandante, pageable);
    }

    @Operation(summary = "Partidas mandante e visitante", description = "Partidas como mandante e visitante de um  clube.")
    @GetMapping("/partidas-clube")
    public Page<Partida> partidasPorClube(
            @RequestParam Long clubeId,
            @RequestParam(required = false) Long estadioId,
            @RequestParam(required = false) Boolean goleada,
            @RequestParam(required = false) Boolean mandante,
            Pageable pageable
    ) {
        return relatorioService.partidasPorClube(clubeId, estadioId, goleada, mandante, pageable);
    }
}

