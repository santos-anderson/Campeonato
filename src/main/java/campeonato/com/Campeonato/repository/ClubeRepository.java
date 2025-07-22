package campeonato.com.Campeonato.repository;

import campeonato.com.Campeonato.entity.Clube;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubeRepository extends JpaRepository<Clube, Long> {
    Optional<Clube> findByNomeAndUfIgnoreCase(String nome, String uf);
}