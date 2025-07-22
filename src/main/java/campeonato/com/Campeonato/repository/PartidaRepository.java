package campeonato.com.Campeonato.repository;

import campeonato.com.Campeonato.entity.Partida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    boolean existsByClubeCasaIdAndDataHoraBetween(Long clubeId, LocalDateTime inicio, LocalDateTime fim);

    boolean existsByClubeVisitanteIdAndDataHoraBetween(Long clubeId, LocalDateTime inicio, LocalDateTime fim);

    boolean existsByEstadioIdAndDataHoraBetween(Long estadioId, LocalDateTime inicio, LocalDateTime fim);

    boolean existsByClubeCasaIdAndDataHoraBetweenAndIdNot(Long clubeId, LocalDateTime inicio, LocalDateTime fim, Long id);

    boolean existsByClubeVisitanteIdAndDataHoraBetweenAndIdNot(Long clubeId, LocalDateTime inicio, LocalDateTime fim, Long id);

    boolean existsByEstadioIdAndDataHoraBetweenAndIdNot(Long estadioId, LocalDateTime ini, LocalDateTime fim, Long id);

}