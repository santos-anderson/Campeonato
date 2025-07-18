package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.ClubeRankingDTO;
import campeonato.com.Campeonato.dto.RetrospectoClubeDTO;
import campeonato.com.Campeonato.dto.RetrospectoContraDTO;
import campeonato.com.Campeonato.services.RelatorioService;
import campeonato.com.Campeonato.exception.PartidaValidacaoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/retrospecto/{clubeId}")
    public ResponseEntity<?> retrospecto(@PathVariable Long clubeId) {
        try {
            RetrospectoClubeDTO retro = relatorioService.retrospectoClube(clubeId);
            return ResponseEntity.ok(retro);
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/retrospecto-contra/{clubeId}")
    public ResponseEntity<?> retrospectoContra(@PathVariable Long clubeId) {
        try {
            List<RetrospectoContraDTO> lista = relatorioService.retrospectoContraAdversarios(clubeId);
            return ResponseEntity.ok(lista);
        } catch (PartidaValidacaoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/confrontos")
    public ResponseEntity<Map<String, Object>> confrontos(
            @RequestParam Long clubeA, @RequestParam Long clubeB) {
        return ResponseEntity.ok(relatorioService.confrontosEntreClubes(clubeA, clubeB));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<ClubeRankingDTO>> ranking(
            @RequestParam(name = "criterio", defaultValue = "pontos") String criterio) {
        List<ClubeRankingDTO> ranking = relatorioService.ranking(criterio);
        return ResponseEntity.ok(ranking);
    }
}