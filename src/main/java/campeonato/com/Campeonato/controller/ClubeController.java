package campeonato.com.Campeonato.controller;

import campeonato.com.Campeonato.dto.ClubeRequestDTO;
import campeonato.com.Campeonato.entity.Clube;
import campeonato.com.Campeonato.services.ClubeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Clubes", description = "Operações relacionadas aos clubes de futebol")
public class ClubeController {

    @Autowired
    private ClubeService clubeService;

    @Operation(summary = "Cadastra um novo clube")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Clube cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (faltando campos obrigatórios, nome com menos de 2 letras, UF inválida ou data futura)"),
            @ApiResponse(responseCode = "409", description = "Já existe um clube com o mesmo nome no mesmo estado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping
    public ResponseEntity<String> cadastrarClube(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados obrigatórios: nome (mín. 2 letras), UF válida, data de criação não futura e status (true = Ativo, false = Inativo."
            )
            @RequestBody @Valid ClubeRequestDTO clubeRequestDTO) {
        String mensagem = clubeService.cadastrarClube(clubeRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(mensagem);
    }

    @Operation(summary = "Atualiza os dados de um clube")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clube atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (nome, UF ou data de criação)"),
            @ApiResponse(responseCode = "404", description = "Clube não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito com dados existentes (nome duplicado no mesmo estado ou data posterior a partidas registradas)"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarClube(
            @Parameter(description = "ID do clube que será atualizado") @PathVariable Long id,
            @RequestBody @Valid ClubeRequestDTO clubeRequestDTO) {
        String mensagem = clubeService.atualizarClube(id, clubeRequestDTO);
        return ResponseEntity.ok(mensagem);
    }

    @Operation(summary = "Inativa um clube (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Clube inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Clube não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativarClube(
            @Parameter(description = "ID do clube que será inativado") @PathVariable Long id) {
        clubeService.inativarClube(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca um clube pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clube encontrado"),
            @ApiResponse(responseCode = "404", description = "Clube não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Clube> buscarClubePorId(
            @Parameter(description = "ID do clube") @PathVariable Long id) {
        Clube clube = clubeService.buscarClubePorId(id);
        return ResponseEntity.ok(clube);
    }

    @Operation(summary = "Lista os clubes com filtros opcionais e paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clubes retornada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping
    public Page<Clube> listarClubes(
            @Parameter(description = "Filtrar por nome") @RequestParam(required = false) String nome,
            @Parameter(description = "Filtrar por UF") @RequestParam(required = false) String uf,
            @Parameter(description = "Filtrar por status (true = ativo, false = inativo)") @RequestParam(required = false) Boolean status,
            @Parameter(description = "Parâmetros de paginação e ordenação")
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return clubeService.listarClubes(nome, uf, status, pageable);
    }
}