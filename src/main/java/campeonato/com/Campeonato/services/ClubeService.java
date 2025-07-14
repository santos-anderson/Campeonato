package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.ClubeRequestDTO;
import campeonato.com.Campeonato.exception.ClubeExisteException;
import campeonato.com.Campeonato.exception.ClubeNaoEncontradoException;
import campeonato.com.Campeonato.model.Clube;
import campeonato.com.Campeonato.repository.ClubeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class ClubeService {

    @Autowired
    private ClubeRepository clubeRepository;

    public String cadastrarClube(ClubeRequestDTO clubeRequestDTO) {
        boolean jaExiste = clubeRepository
                .findByNomeAndUfIgnoreCase(clubeRequestDTO.getNome(), clubeRequestDTO.getUf())
                .isPresent();

        if (jaExiste) {
            throw new ClubeExisteException("Já existe um clube com esse nome nesse estado.");
        }

        Clube clube = new Clube();
        clube.setNome(clubeRequestDTO.getNome());
        clube.setUf(clubeRequestDTO.getUf());
        clube.setDataCriacao(clubeRequestDTO.getDataCriacao());
        clube.setStatus(clubeRequestDTO.getStatus());

        clubeRepository.save(clube);

        return "Clube " + clube.getNome() + " cadastrado com sucesso!";
    }

    public String atualizarClube(Long id, ClubeRequestDTO dto) {
        Clube clube = clubeRepository.findById(id)
                .orElseThrow(() -> new ClubeNaoEncontradoException("Clube não encontrado!"));

        clubeRepository.findByNomeAndUfIgnoreCase(dto.getNome(), dto.getUf())
                .filter(outroClube -> !outroClube.getId().equals(id))
                .ifPresent(outroClube -> {
                    throw new ClubeExisteException("Já existe um clube com esse nome nesse estado.");
                });

        clube.setNome(dto.getNome());
        clube.setUf(dto.getUf());
        clube.setDataCriacao(dto.getDataCriacao());
        clube.setStatus(dto.getStatus());

        clubeRepository.save(clube);
        return "Clube atualizado com sucesso!";
    }

    public void inativarClube(Long id) {
        Clube clube = clubeRepository.findById(id)
                .orElseThrow(() -> new ClubeNaoEncontradoException("Clube não encontrado!"));

        if (!Boolean.FALSE.equals(clube.getStatus())) {
            clube.setStatus(false);
            clubeRepository.save(clube);
        }
    }

    public Clube buscarClubePorId(Long id) {
        return clubeRepository.findById(id).orElseThrow(() ->
                new ClubeNaoEncontradoException("Clube não encontrado!"));
    }


    public Page<Clube> listarClubes(String nome, String uf, Boolean status, Pageable pageable) {
        List<Clube> clubes = clubeRepository.findAll();

        if (nome != null) {
            clubes = clubes.stream()
                    .filter(c -> c.getNome().toLowerCase().contains(nome.toLowerCase()))
                    .toList();
        }
        if (uf != null) {
            clubes = clubes.stream()
                    .filter(c -> c.getUf().equalsIgnoreCase(uf))
                    .toList();
        }
        if (status != null) {
            clubes = clubes.stream()
                    .filter(c -> c.getStatus().equals(status))
                    .toList();
        }


        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), clubes.size());
        List<Clube> pagina = (start >= clubes.size()) ? List.of() : clubes.subList(start, end);

        return new PageImpl<>(pagina, pageable, clubes.size());
    }
}


