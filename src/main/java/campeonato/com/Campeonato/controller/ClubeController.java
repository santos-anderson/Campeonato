package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.ClubeRequestDTO;
import campeonato.com.Campeonato.entity.Clube;
import campeonato.com.Campeonato.services.ClubeService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/clube")
public class ClubeController {

    @Autowired
    private ClubeService clubeService;

    @Operation(summary = "Cadastra Clube", description = "Cadastro de um novo clube de futebol com validação dos dados!.")
    @PostMapping
    public ResponseEntity<String> cadastrarClube(@RequestBody @Valid ClubeRequestDTO clubeRequestDTO) {
        String mensagem = clubeService.cadastrarClube(clubeRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(mensagem);
    }

    @Operation(summary = "Atualiza Clube", description = "Atualiza um clube de futebol com validação dos dados informados!.")
    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarClube(@PathVariable Long id, @RequestBody @Valid ClubeRequestDTO clubeRequestDTO) {
        String mensagem = clubeService.atualizarClube(id, clubeRequestDTO);
        return ResponseEntity.ok(mensagem);
    }


    @Operation(summary = "Inativa Clube", description = "Inativa um clube de futebol!.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativarClube(@PathVariable Long id) {
        clubeService.inativarClube(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Busca Clube", description = "Busca Clube pelo ID informado.")
    @GetMapping("/{id}")
    public ResponseEntity<Clube> buscarClubePorId(@PathVariable Long id) {
        Clube clube = clubeService.buscarClubePorId(id);
        return ResponseEntity.ok(clube);
    }

    @Operation(summary = "Lista Clubes", description = "Lista os Clubes e lista o clube com os parametros informados.")
    @GetMapping
    public Page<Clube> listarClubes(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) Boolean status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return clubeService.listarClubes(nome, uf, status, pageable);
    }
}
