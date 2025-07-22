package campeonato.com.Campeonato.services;

import campeonato.com.Campeonato.dto.ClubeRankingDTO;
import campeonato.com.Campeonato.dto.RetrospectoClubeDTO;
import campeonato.com.Campeonato.dto.RetrospectoContraDTO;
import campeonato.com.Campeonato.exception.PartidaValidacaoException;
import campeonato.com.Campeonato.entity.Clube;
import campeonato.com.Campeonato.entity.Partida;
import campeonato.com.Campeonato.repository.ClubeRepository;
import campeonato.com.Campeonato.repository.PartidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;



@Service
public class RelatorioService {

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private ClubeRepository clubeRepository;

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

    public Page<Partida> partidasGoleada(Long clubeId, Long estadioId, Boolean mandante, Pageable pageable) {
        List<Partida> partidas = partidaRepository.findAll();

        if (clubeId != null) {
            if (mandante == null) {
                partidas = partidas.stream()
                        .filter(p -> p.getClubeCasa().getId().equals(clubeId) || p.getClubeVisitante().getId().equals(clubeId))
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


        partidas = partidas.stream()
                .filter(p -> Math.abs(p.getGolsCasa() - p.getGolsVisitante()) >= 3)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), partidas.size());
        List<Partida> pagina = start >= partidas.size() ? List.of() : partidas.subList(start, end);

        return new PageImpl<>(pagina, pageable, partidas.size());
    }

    public Page<Partida> partidasPorClube(Long clubeId, Long estadioId, Boolean goleada, Boolean mandante, Pageable pageable) {
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


    if (pageable.getSort().isSorted()) {
        for (Sort.Order order : pageable.getSort()) {
            if (order.getProperty().equalsIgnoreCase("dataHora")) {
                Comparator<Partida> comparator = Comparator.comparing(Partida::getDataHora);
                if (order.getDirection().isDescending()) {
                    comparator = comparator.reversed();
                }
                partidas = partidas.stream().sorted(comparator).toList();
            }

        }
    }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), partidas.size());
        List<Partida> pagina = (start >= partidas.size()) ? List.of() : partidas.subList(start, end);

        return new PageImpl<>(pagina, pageable, partidas.size());
    }
}