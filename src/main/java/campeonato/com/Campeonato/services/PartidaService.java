package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.ClubeRankingDTO;
import campeonato.com.Campeonato.dto.RetrospectoClubeDTO;
import campeonato.com.Campeonato.dto.RetrospectoContraDTO;
import campeonato.com.Campeonato.model.Partida;
import campeonato.com.Campeonato.repository.PartidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import campeonato.com.Campeonato.dto.PartidaRequestDTO;
import campeonato.com.Campeonato.exception.PartidaCadastroException;
import campeonato.com.Campeonato.exception.PartidaValidacaoException;
import campeonato.com.Campeonato.model.Clube;
import campeonato.com.Campeonato.model.Estadio;
import campeonato.com.Campeonato.repository.ClubeRepository;
import campeonato.com.Campeonato.repository.EstadioRepository;
import org.springframework.web.server.ResponseStatusException;


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

    public RetrospectoClubeDTO retrospectoClube(Long clubeId) {

        Clube clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new PartidaValidacaoException("Clube não encontrado!"));


        List<Partida> partidas = partidaRepository.findAll().stream()
                .filter(p -> p.getClubeCasa().getId().equals(clubeId)
                        || p.getClubeVisitante().getId().equals(clubeId))
                .toList();


        int vitorias = (int) partidas.stream().filter(p -> {
            boolean casa = p.getClubeCasa().getId().equals(clubeId);
            int meusGols = casa ? p.getGolsCasa() : p.getGolsVisitante();
            int golsOponente = casa ? p.getGolsVisitante() : p.getGolsCasa();
            return meusGols > golsOponente;
        }).count();

        int empates = (int) partidas.stream().filter(p -> {
            boolean casa = p.getClubeCasa().getId().equals(clubeId);
            int meusGols = casa ? p.getGolsCasa() : p.getGolsVisitante();
            int golsOponente = casa ? p.getGolsVisitante() : p.getGolsCasa();
            return meusGols == golsOponente;
        }).count();

        int derrotas = (int) partidas.stream().filter(p -> {
            boolean casa = p.getClubeCasa().getId().equals(clubeId);
            int meusGols = casa ? p.getGolsCasa() : p.getGolsVisitante();
            int golsOponente = casa ? p.getGolsVisitante() : p.getGolsCasa();
            return meusGols < golsOponente;
        }).count();

        int golsFeitos = partidas.stream().mapToInt(p -> {
            boolean casa = p.getClubeCasa().getId().equals(clubeId);
            return casa ? p.getGolsCasa() : p.getGolsVisitante();
        }).sum();

        int golsSofridos = partidas.stream().mapToInt(p -> {
            boolean casa = p.getClubeCasa().getId().equals(clubeId);
            return casa ? p.getGolsVisitante() : p.getGolsCasa();
        }).sum();

        return new RetrospectoClubeDTO(vitorias, empates, derrotas, golsFeitos, golsSofridos);
    }

    public List<RetrospectoContraDTO> retrospectoContraAdversarios(Long clubeId) {

        Clube clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new PartidaValidacaoException("Clube não encontrado!"));


        List<Partida> partidas = partidaRepository.findAll().stream()
                .filter(p -> p.getClubeCasa().getId().equals(clubeId)
                        || p.getClubeVisitante().getId().equals(clubeId))
                .toList();


        Map<Long, List<Partida>> porAdversario = new HashMap<>();

        for (Partida p : partidas) {
            Long adversarioId;
            String adversarioNome;
            if (p.getClubeCasa().getId().equals(clubeId)) {
                adversarioId = p.getClubeVisitante().getId();
                adversarioNome = p.getClubeVisitante().getNome();
            } else {
                adversarioId = p.getClubeCasa().getId();
                adversarioNome = p.getClubeCasa().getNome();
            }
            porAdversario.computeIfAbsent(adversarioId, k -> new ArrayList<>()).add(p);
        }


        List<RetrospectoContraDTO> lista = new ArrayList<>();
        for (Map.Entry<Long, List<Partida>> entry : porAdversario.entrySet()) {
            Long adversarioId = entry.getKey();
            List<Partida> jogos = entry.getValue();
            String adversarioNome = null;
            int vitorias = 0, empates = 0, derrotas = 0, golsFeitos = 0, golsSofridos = 0;
            for (Partida p : jogos) {
                boolean casa = p.getClubeCasa().getId().equals(clubeId);
                if (adversarioNome == null) {
                    adversarioNome = (casa ? p.getClubeVisitante().getNome() : p.getClubeCasa().getNome());
                }
                int meusGols = casa ? p.getGolsCasa() : p.getGolsVisitante();
                int golsAdversario = casa ? p.getGolsVisitante() : p.getGolsCasa();
                golsFeitos += meusGols;
                golsSofridos += golsAdversario;
                if (meusGols > golsAdversario) vitorias++;
                else if (meusGols == golsAdversario) empates++;
                else derrotas++;
            }
            lista.add(new RetrospectoContraDTO(adversarioId, adversarioNome,
                    vitorias, empates, derrotas, golsFeitos, golsSofridos));
        }

        return lista;
    }


    public Map<String, Object> confrontosEntreClubes(Long clubeIdA, Long clubeIdB) {

        Clube clubeA = clubeRepository.findById(clubeIdA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clube A não encontrado!"));
        Clube clubeB = clubeRepository.findById(clubeIdB)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clube B não encontrado!"));


        List<Partida> partidas = partidaRepository.findAll().stream()
                .filter(p -> (p.getClubeCasa().getId().equals(clubeIdA) && p.getClubeVisitante().getId().equals(clubeIdB))
                        || (p.getClubeCasa().getId().equals(clubeIdB) && p.getClubeVisitante().getId().equals(clubeIdA)))
                .toList();

        int vitoriasA = 0, empates = 0, derrotasA = 0, golsA = 0, golsB = 0;
        for (Partida p : partidas) {
            boolean aCasa = p.getClubeCasa().getId().equals(clubeIdA);
            int golsClubeA = aCasa ? p.getGolsCasa() : p.getGolsVisitante();
            int golsClubeB = aCasa ? p.getGolsVisitante() : p.getGolsCasa();
            golsA += golsClubeA;
            golsB += golsClubeB;
            if (golsClubeA > golsClubeB) vitoriasA++;
            else if (golsClubeA == golsClubeB) empates++;
            else derrotasA++;
        }

        int vitoriasB = derrotasA;
        int derrotasB = vitoriasA;
        int golsFeitosB = golsB, golsSofridosB = golsA;
        int golsFeitosA = golsA, golsSofridosA = golsB;

        RetrospectoContraDTO retroA = new RetrospectoContraDTO(
                clubeB.getId(), clubeB.getNome(), vitoriasA, empates, derrotasA, golsFeitosA, golsSofridosA);
        RetrospectoContraDTO retroB = new RetrospectoContraDTO(
                clubeA.getId(), clubeA.getNome(), vitoriasB, empates, derrotasB, golsFeitosB, golsSofridosB);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("partidas", partidas);
        resultado.put("retrospectoA", retroA);
        resultado.put("retrospectoB", retroB);
        return resultado;
    }

    public List<ClubeRankingDTO> ranking(String criterio) {
        List<Clube> clubes = clubeRepository.findAll();
        List<Partida> partidas = partidaRepository.findAll();
        List<ClubeRankingDTO> ranking = new ArrayList<>();
        for (Clube c : clubes) {
            int vitorias = 0, empates = 0, golsFeitos = 0, jogos = 0;
            for (Partida p : partidas) {
                boolean casa = p.getClubeCasa().getId().equals(c.getId());
                boolean visitante = p.getClubeVisitante().getId().equals(c.getId());
                if (!casa && !visitante) continue;
                jogos++;
                int meusGols = casa ? p.getGolsCasa() : p.getGolsVisitante();
                int golsAdv = casa ? p.getGolsVisitante() : p.getGolsCasa();
                golsFeitos += meusGols;
                if (meusGols > golsAdv) vitorias++;
                else if (meusGols == golsAdv) empates++;
            }
            int pontos = 3 * vitorias + empates;
            boolean incluir = switch (criterio) {
                case "pontos"   -> pontos > 0;
                case "gols"     -> golsFeitos > 0;
                case "vitorias" -> vitorias > 0;
                case "jogos"    -> jogos > 0;
                default         -> false;
            };
            if (incluir)
                ranking.add(new ClubeRankingDTO(
                        c.getId(), c.getNome(), pontos, golsFeitos, vitorias, jogos));
        }
        Comparator<ClubeRankingDTO> comparator = switch (criterio) {
            case "pontos"   -> Comparator.comparingInt(ClubeRankingDTO::getPontos).reversed();
            case "gols"     -> Comparator.comparingInt(ClubeRankingDTO::getGolsFeitos).reversed();
            case "vitorias" -> Comparator.comparingInt(ClubeRankingDTO::getVitorias).reversed();
            case "jogos"    -> Comparator.comparingInt(ClubeRankingDTO::getJogos).reversed();
            default         -> Comparator.comparingInt(ClubeRankingDTO::getPontos).reversed();
        };
        ranking.sort(comparator);
        return ranking;
    }
}
