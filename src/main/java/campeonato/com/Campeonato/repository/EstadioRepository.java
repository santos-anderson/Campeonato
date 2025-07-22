package campeonato.com.Campeonato.repository;

import campeonato.com.Campeonato.entity.Estadio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadioRepository extends JpaRepository<Estadio, Long> {
    Optional<Estadio> findByNomeIgnoreCase(String nome);
}
