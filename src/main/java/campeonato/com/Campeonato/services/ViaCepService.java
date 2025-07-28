package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.ViaCepResquestDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class ViaCepService {

    private final RestTemplate restTemplate = new RestTemplate();

    public ViaCepResquestDTO consultarCep(String cep) {

        String url = UriComponentsBuilder
                .fromHttpUrl("https://viacep.com.br/ws/" + cep + "/json/")
                .toUriString();

        ViaCepResquestDTO response = restTemplate.getForObject(url, ViaCepResquestDTO.class);

        if (response == null || Boolean.TRUE.equals(response.getErro())) {
            throw new RuntimeException("CEP não encontrado ou inválido!");
        }

        return response;
    }
}