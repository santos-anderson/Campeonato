package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.ClubeRequestDTO;
import campeonato.com.Campeonato.exception.ClubeExisteException;
import campeonato.com.Campeonato.exception.ClubeNaoEncontradoException;
import campeonato.com.Campeonato.model.Clube;
import campeonato.com.Campeonato.services.ClubeService;
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

    @PostMapping
    public ResponseEntity<String> cadastrarClube(@RequestBody @Valid ClubeRequestDTO clubeRequestDTO) {
        try {
            String mensagem = clubeService.cadastrarClube(clubeRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(mensagem);
        } catch (ClubeExisteException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarClube(
            @PathVariable Long id,
            @RequestBody @Valid ClubeRequestDTO clubeRequestDTO) {
        try {
            String mensagem = clubeService.atualizarClube(id, clubeRequestDTO);
            return ResponseEntity.ok(mensagem);
        } catch (ClubeExisteException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (ClubeNaoEncontradoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> inativarClube(@PathVariable Long id) {
        try {
            clubeService.inativarClube(id);
            return ResponseEntity.noContent().build();
        } catch (ClubeNaoEncontradoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarClubePorId(@PathVariable Long id) {
        try {
            Clube clube = clubeService.buscarClubePorId(id);
            return ResponseEntity.ok(clube);
        } catch (ClubeNaoEncontradoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

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



