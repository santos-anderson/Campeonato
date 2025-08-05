package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.EstadioRequestDTO;
import campeonato.com.Campeonato.entity.Estadio;
import campeonato.com.Campeonato.services.EstadioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estadio")
@Tag(name = "Estádios", description = "Operações relacionadas aos estádios de futebol")
public class EstadioController {

    @Autowired
    private EstadioService estadioService;

    @Operation(summary = "Cadastra um novo estádio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estádio cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: nome menor que 3 letras)"),
            @ApiResponse(responseCode = "409", description = "Estádio com nome já existente"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping
    public ResponseEntity<String> cadastrarEstadio(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados obrigatórios: nome do estádio (mínimo 3 letras)")
            @RequestBody @Valid EstadioRequestDTO estadioRequestDTO) {
        String mensagem = estadioService.cadastrarEstadio(estadioRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(mensagem);
    }

    @Operation(summary = "Atualiza os dados de um estádio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estádio atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nome inválido (ex: menor que 3 letras)"),
            @ApiResponse(responseCode = "404", description = "Estádio não encontrado"),
            @ApiResponse(responseCode = "409", description = "Nome já utilizado por outro estádio"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarEstadio(
            @Parameter(description = "ID do estádio a ser atualizado") @PathVariable Long id,
            @RequestBody @Valid EstadioRequestDTO estadioRequestDTO) {
        String mensagem = estadioService.atualizaEstadio(id, estadioRequestDTO);
        return ResponseEntity.ok(mensagem);
    }


    @Operation(summary = "Busca um estádio pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estádio encontrado"),
            @ApiResponse(responseCode = "404", description = "Estádio não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Estadio> buscarEstadioPorId(
            @Parameter(description = "ID do estádio a ser buscado") @PathVariable Long id) {
        Estadio estadio = estadioService.buscarEstadioPorId(id);
        return ResponseEntity.ok(estadio);
    }

    @Operation(summary = "Lista estádios com opção de filtro por nome e paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping
    public Page<Estadio> listarEstadio(
            @Parameter(description = "Filtro por nome do estádio") @RequestParam(required = false) String nome,
            @Parameter(description = "Parâmetros de paginação e ordenação")
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return estadioService.listarEstadio(nome, pageable);
    }
}
