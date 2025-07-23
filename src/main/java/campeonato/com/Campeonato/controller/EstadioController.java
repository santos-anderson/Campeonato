package campeonato.com.Campeonato.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import campeonato.com.Campeonato.dto.EstadioRequestDTO;
import campeonato.com.Campeonato.exception.EstadioExisteException;
import campeonato.com.Campeonato.exception.EstadioNaoEncontradoException;
import campeonato.com.Campeonato.entity.Estadio;
import campeonato.com.Campeonato.services.EstadioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estadio")
public class EstadioController {


    @Autowired
    private EstadioService estadioService;

    @Operation(summary = "Cadastra Estadio", description = "Cadastra um novo Estadio de futebol com validação dos dados informados.")
    @PostMapping
    public ResponseEntity<String> cadastrarEstadio(@RequestBody @Valid EstadioRequestDTO estadioRequestDTO) {
        try {
            String mensagem = estadioService.cadastrarEstadio(estadioRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(mensagem);
        } catch (EstadioExisteException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    @Operation(summary = "Atualiza Estadio", description = "Atualiza um estadio de futebol com validação dos dados informados.")
    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarEstadio(
            @PathVariable Long id,
            @RequestBody @Valid EstadioRequestDTO estadioRequestDTO) {
        try {
            String mensagem = estadioService.atualizaEstadio(id, estadioRequestDTO);
            return ResponseEntity.ok(mensagem);
        } catch (EstadioExisteException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (EstadioNaoEncontradoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @Operation(summary = "Busca Estadios", description = "Busca um estadio de futebol por ID.")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarEstadioPorId(@PathVariable Long id) {
        try {
            Estadio estadio = estadioService.buscarEstadioPorId(id);
            return ResponseEntity.ok(estadio);
        } catch (EstadioNaoEncontradoException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }
    @Operation(summary = "Lista Estadios", description = "Lista Estadios, e Lista o Estadio pelo nome informado.")
    @GetMapping
    public Page<Estadio> listarEstadio(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return estadioService.listarEstadio(nome, pageable);
    }
}