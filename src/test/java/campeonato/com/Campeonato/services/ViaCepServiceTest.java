package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.ViaCepResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ViaCepServiceTest {

    private RestTemplate restTemplate;
    private ViaCepService viaCepService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);

        viaCepService = new ViaCepService();


        try {
            java.lang.reflect.Field field = ViaCepService.class.getDeclaredField("restTemplate");
            field.setAccessible(true);
            field.set(viaCepService, restTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void deveLancarExcecaoQuandoResponseNull() {
        when(restTemplate.getForObject(anyString(), eq(ViaCepResponseDTO.class)))
                .thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> viaCepService.consultarCep("00000-000"));
        assertEquals("CEP não encontrado ou inválido!", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoErroTrue() {
        ViaCepResponseDTO response = new ViaCepResponseDTO();
        response.setErro(true);

        when(restTemplate.getForObject(anyString(), eq(ViaCepResponseDTO.class)))
                .thenReturn(response);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> viaCepService.consultarCep("00000-000"));
        assertEquals("CEP não encontrado ou inválido!", ex.getMessage());
    }

    @Test
    void deveRetornarResponseValido() {
        ViaCepResponseDTO response = new ViaCepResponseDTO();
        response.setLogradouro("Teste");
        response.setBairro("Bairro");
        response.setErro(false);

        when(restTemplate.getForObject(anyString(), eq(ViaCepResponseDTO.class)))
                .thenReturn(response);

        ViaCepResponseDTO res = viaCepService.consultarCep("12345-678");
        assertEquals("Teste", res.getLogradouro());
        assertFalse(Boolean.TRUE.equals(res.getErro()));
    }
}