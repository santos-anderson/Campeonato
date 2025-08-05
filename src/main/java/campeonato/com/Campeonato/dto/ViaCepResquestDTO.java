package campeonato.com.Campeonato.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ViaCepResquestDTO {

    @Schema(description = "CEP", example = "01001-000")
    private String cep;

    @Schema(description = "Logradouro", example = "Praça da Sé")
    private String logradouro;

    @Schema(description = "Complemento", example = "lado ímpar")
    private String complemento;

    @Schema(description = "Bairro", example = "Sé")
    private String bairro;

    @Schema(description = "Localidade (Cidade)", example = "São Paulo")
    private String localidade;

    @Schema(description = "UF (Estado)", example = "SP")
    private String uf;

    @Schema(description = "Código IBGE", example = "3550308")
    private String ibge;

    @Schema(description = "Código GIA", example = "1004")
    private String gia;

    @Schema(description = "DDD do telefone", example = "11")
    private String ddd;

    @Schema(description = "Código SIAFI", example = "7107")
    private String siafi;

    @Schema(description = "Indica erro na consulta", example = "false")
    private Boolean erro;
}