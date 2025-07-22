package campeonato.com.Campeonato.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "partidas")
public class Partida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="clube_casa_id")
    private Clube clubeCasa;
    @ManyToOne
    @JoinColumn(name="clube_visitante_id")
    private Clube clubeVisitante;
    @ManyToOne
    @JoinColumn(name="estadio_id")
    private Estadio estadio;
    @Column(name="data_hora")
    private LocalDateTime dataHora;
    @Column(name="gols_casa")
    private Integer golsCasa;
    @Column(name = "gols_visitante")
    private Integer golsVisitante;

}