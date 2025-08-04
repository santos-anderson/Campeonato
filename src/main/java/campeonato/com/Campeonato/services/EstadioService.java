package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.EstadioRequestDTO;
import campeonato.com.Campeonato.dto.ViaCepResquestDTO;
import campeonato.com.Campeonato.entity.Estadio;
import campeonato.com.Campeonato.exception.EstadioExisteException;
import campeonato.com.Campeonato.exception.EstadioNaoEncontradoException;
import campeonato.com.Campeonato.repository.EstadioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EstadioService {

    @Autowired
    private EstadioRepository estadioRepository;

    @Autowired
    private ViaCepService viaCepService;

    public String cadastrarEstadio(EstadioRequestDTO estadioRequestDTO) {
        boolean jaExiste = estadioRepository
                .findByNomeIgnoreCase(estadioRequestDTO.getNome())
                .isPresent();

        if (jaExiste) {
            throw new EstadioExisteException("Já existe um Estádio com esse nome!.");
        }

        ViaCepResquestDTO endereco = viaCepService.consultarCep(estadioRequestDTO.getCep());

        Estadio estadio = new Estadio();
        estadio.setNome(estadioRequestDTO.getNome().trim());
        estadio.setCep(estadioRequestDTO.getCep());
        estadio.setLogradouro(endereco.getLogradouro());
        estadio.setBairro(endereco.getBairro());
        estadio.setLocalidade(endereco.getLocalidade());
        estadio.setUf(endereco.getUf());

        estadioRepository.save(estadio);

        return "Estadio ID : " + estadio.getId()+
                "\nEstádio : " + estadio.getNome()+
                "\nEndereço : " + estadio.getLogradouro()+
                "\nBairro : " + estadio.getBairro()+
                "\nLocalidade : "+estadio.getLocalidade()+
                "\nUF : "+estadio.getUf()+
                "\nCep : " + estadio.getCep()+
                "\nCadastrado com sucesso!";
    }

    public String atualizaEstadio(Long id, EstadioRequestDTO dto) {
        Estadio estadio = estadioRepository.findById(id)
                .orElseThrow(() -> new EstadioNaoEncontradoException("Estádio não encontrado!"));

        estadioRepository.findByNomeIgnoreCase(dto.getNome())
                .filter(outroEstadio -> !outroEstadio.getId().equals(id))
                .ifPresent(outroEstadio -> {
                    throw new EstadioExisteException("Já existe um estádio com esse nome!");
                });

        estadio.setNome(dto.getNome());
        estadio.setCep(dto.getCep());

        ViaCepResquestDTO endereco = viaCepService.consultarCep(dto.getCep());
        estadio.setLogradouro(endereco.getLogradouro());
        estadio.setBairro(endereco.getBairro());
        estadio.setLocalidade(endereco.getLocalidade());
        estadio.setUf(endereco.getUf());

        estadioRepository.save(estadio);
        return "Estádio atualizado com sucesso!";
    }

    public Estadio buscarEstadioPorId(Long id) {
        return estadioRepository.findById(id).orElseThrow(() ->
                new EstadioNaoEncontradoException("Estádio não encontrado!"));
    }

    public Page<Estadio> listarEstadio(String nome, Pageable pageable) {
        List<Estadio> estadios = estadioRepository.findAll();

        if (nome != null) {
            estadios = estadios.stream()
                    .filter(e -> e.getNome().toLowerCase().contains(nome.toLowerCase()))
                    .toList();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), estadios.size());
        List<Estadio> pagina = (start >= estadios.size()) ? List.of() : estadios.subList(start, end);

        return new PageImpl<>(pagina, pageable, estadios.size());
    }
}