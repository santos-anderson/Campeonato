package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.entity.Partida;
import campeonato.com.Campeonato.repository.PartidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import campeonato.com.Campeonato.exception.PartidaCadastroException;
import campeonato.com.Campeonato.exception.PartidaValidacaoException;
import campeonato.com.Campeonato.entity.Clube;
import campeonato.com.Campeonato.entity.Estadio;
import campeonato.com.Campeonato.repository.ClubeRepository;
import campeonato.com.Campeonato.repository.EstadioRepository;


import java.time.LocalDateTime;
import java.util.*;

@Service
public class PartidaService {

    @Autowired
    private PartidaRepository partidaRepository;
    @Autowired
    private ClubeRepository clubeRepository;
    @Autowired
    private EstadioRepository estadioRepository;

    public String cadastrarPartida(PartidaRequestDTO partidaRequestDTO) {

        if (partidaRequestDTO.getClubeCasaId() == null || partidaRequestDTO.getClubeVisitanteId() == null ||
                partidaRequestDTO.getEstadioId() == null || partidaRequestDTO.getDataHora() == null ||
                partidaRequestDTO.getGolsCasa() == null || partidaRequestDTO.getGolsVisitante() == null) {
            throw new PartidaValidacaoException("Todos os campos são obrigatórios.");
        }
        if (partidaRequestDTO.getClubeCasaId().equals(partidaRequestDTO.getClubeVisitanteId())) {
            throw new PartidaValidacaoException("Clubes não podem ser iguais!");
        }
        if (partidaRequestDTO.getGolsCasa() < 0 || partidaRequestDTO.getGolsVisitante() < 0) {
            throw new PartidaValidacaoException("Gols não podem ser negativos!");
        }

        Clube casa = clubeRepository.findById(partidaRequestDTO.getClubeCasaId()).orElse(null);
        Clube visitante = clubeRepository.findById(partidaRequestDTO.getClubeVisitanteId()).orElse(null);
        Estadio estadio = estadioRepository.findById(partidaRequestDTO.getEstadioId()).orElse(null);

        if (casa == null)
            throw new PartidaValidacaoException("Clube da casa não existe!");
        if (visitante == null)
            throw new PartidaValidacaoException("Clube visitante não existe!");
        if (estadio == null)
            throw new PartidaValidacaoException("Estádio não existe!");


        if (!Boolean.TRUE.equals(casa.getStatus()))
            throw new PartidaCadastroException("Clube da casa está inativo.");
        if (!Boolean.TRUE.equals(visitante.getStatus()))
            throw new PartidaCadastroException("Clube visitante está inativo.");

        if (partidaRequestDTO.getDataHora().toLocalDate().isBefore(casa.getDataCriacao()) ||
                partidaRequestDTO.getDataHora().toLocalDate().isBefore(visitante.getDataCriacao()))
            throw new PartidaCadastroException("Data da partida não pode ser anterior à data de criação de algum clube.");


        LocalDateTime inicio = partidaRequestDTO.getDataHora().minusHours(48);
        LocalDateTime fim = partidaRequestDTO.getDataHora().plusHours(48);
        if (partidaRepository.existsByClubeCasaIdAndDataHoraBetween(casa.getId(), inicio, fim) ||
                partidaRepository.existsByClubeVisitanteIdAndDataHoraBetween(casa.getId(), inicio, fim) ||
                partidaRepository.existsByClubeCasaIdAndDataHoraBetween(visitante.getId(), inicio, fim) ||
                partidaRepository.existsByClubeVisitanteIdAndDataHoraBetween(visitante.getId(), inicio, fim)) {
            throw new PartidaCadastroException("Um dos clubes já tem partida marcada em menos de 48h.");
        }


        LocalDateTime diaIni = partidaRequestDTO.getDataHora().toLocalDate().atStartOfDay();
        LocalDateTime diaFim = partidaRequestDTO.getDataHora().toLocalDate().atTime(23, 59, 59);
        if (partidaRepository.existsByEstadioIdAndDataHoraBetween(estadio.getId(), diaIni, diaFim))
            throw new PartidaCadastroException("Já existe partida nesse estádio nesse dia!");


        Partida partida = new Partida();
        partida.setClubeCasa(casa);
        partida.setClubeVisitante(visitante);
        partida.setEstadio(estadio);
        partida.setDataHora(partidaRequestDTO.getDataHora());
        partida.setGolsCasa(partidaRequestDTO.getGolsCasa());
        partida.setGolsVisitante(partidaRequestDTO.getGolsVisitante());

        partidaRepository.save(partida);
        return "Partida cadastrada!";
    }

    public String atualizarPartida(Long id, PartidaRequestDTO dto) {
        Partida partida = partidaRepository.findById(id)
                .orElseThrow(() -> new PartidaValidacaoException("Partida não encontrada!"));

        if (dto.getClubeCasaId() == null || dto.getClubeVisitanteId() == null ||
                dto.getEstadioId() == null || dto.getDataHora() == null ||
                dto.getGolsCasa() == null || dto.getGolsVisitante() == null) {
            throw new PartidaValidacaoException("Todos os campos são obrigatórios.");
        }
        if (dto.getClubeCasaId().equals(dto.getClubeVisitanteId())) {
            throw new PartidaValidacaoException("Clubes não podem ser iguais!");
        }
        if (dto.getGolsCasa() < 0 || dto.getGolsVisitante() < 0) {
            throw new PartidaValidacaoException("Gols não podem ser negativos!");
        }

        Clube casa = clubeRepository.findById(dto.getClubeCasaId()).orElse(null);
        Clube visitante = clubeRepository.findById(dto.getClubeVisitanteId()).orElse(null);
        Estadio estadio = estadioRepository.findById(dto.getEstadioId()).orElse(null);

        if (casa == null)
            throw new PartidaValidacaoException("Clube da casa não existe!");
        if (visitante == null)
            throw new PartidaValidacaoException("Clube visitante não existe!");
        if (estadio == null)
            throw new PartidaValidacaoException("Estádio não existe!");

        if (!Boolean.TRUE.equals(casa.getStatus()))
            throw new PartidaCadastroException("Clube da casa está inativo.");
        if (!Boolean.TRUE.equals(visitante.getStatus()))
            throw new PartidaCadastroException("Clube visitante está inativo.");

        if (dto.getDataHora().toLocalDate().isBefore(casa.getDataCriacao()) ||
                dto.getDataHora().toLocalDate().isBefore(visitante.getDataCriacao()))
            throw new PartidaCadastroException("Data da partida não pode ser anterior à data de criação de algum clube.");

        LocalDateTime inicio = dto.getDataHora().minusHours(48);
        LocalDateTime fim = dto.getDataHora().plusHours(48);
        if (partidaRepository.existsByClubeCasaIdAndDataHoraBetweenAndIdNot(casa.getId(), inicio, fim, id) ||
                partidaRepository.existsByClubeVisitanteIdAndDataHoraBetweenAndIdNot(casa.getId(), inicio, fim, id) ||
                partidaRepository.existsByClubeCasaIdAndDataHoraBetweenAndIdNot(visitante.getId(), inicio, fim, id) ||
                partidaRepository.existsByClubeVisitanteIdAndDataHoraBetweenAndIdNot(visitante.getId(), inicio, fim, id)) {
            throw new PartidaCadastroException("Um dos clubes já tem partida marcada em menos de 48h!");
        }

        LocalDateTime diaIni = dto.getDataHora().toLocalDate().atStartOfDay();
        LocalDateTime diaFim = dto.getDataHora().toLocalDate().atTime(23, 59, 59);
        if (partidaRepository.existsByEstadioIdAndDataHoraBetweenAndIdNot(estadio.getId(), diaIni, diaFim, id))
            throw new PartidaCadastroException("Já existe partida nesse estádio nesse dia!");

        partida.setClubeCasa(casa);
        partida.setClubeVisitante(visitante);
        partida.setEstadio(estadio);
        partida.setDataHora(dto.getDataHora());
        partida.setGolsCasa(dto.getGolsCasa());
        partida.setGolsVisitante(dto.getGolsVisitante());

        partidaRepository.save(partida);
        return "Partida atualizada com sucesso!";
    }

    public void removerPartida(Long id) {
        Partida partida = partidaRepository.findById(id)
                .orElseThrow(() -> new PartidaValidacaoException("Partida não encontrada!"));
        partidaRepository.delete(partida);
    }

    public Partida buscarPartida(Long id) {
        return partidaRepository.findById(id)
                .orElseThrow(() -> new PartidaValidacaoException("Partida não encontrada"));
    }

    public Page<Partida> listarPartidas(Long clubeId, Long estadioId, Boolean goleada, Boolean mandante, Pageable pageable) {
        List<Partida> partidas = partidaRepository.findAll();


        if (clubeId != null) {
            if (mandante == null) {
                partidas = partidas.stream()
                        .filter(p -> p.getClubeCasa().getId().equals(clubeId)
                                || p.getClubeVisitante().getId().equals(clubeId))
                        .toList();
            } else if (mandante) {
                partidas = partidas.stream()
                        .filter(p -> p.getClubeCasa().getId().equals(clubeId))
                        .toList();
            } else {
                partidas = partidas.stream()
                        .filter(p -> p.getClubeVisitante().getId().equals(clubeId))
                        .toList();
            }
        }


        if (estadioId != null) {
            partidas = partidas.stream()
                    .filter(p -> p.getEstadio().getId().equals(estadioId))
                    .toList();
        }


        if (goleada != null && goleada) {
            partidas = partidas.stream()
                    .filter(p -> Math.abs(p.getGolsCasa() - p.getGolsVisitante()) >= 3)
                    .toList();
        }

        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), partidas.size());
        List<Partida> pagina = (start >= partidas.size()) ? List.of() : partidas.subList(start, end);

        return new PageImpl<>(pagina, pageable, partidas.size());
    }
}
