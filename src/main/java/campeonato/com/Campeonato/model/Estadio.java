package campeonato.com.Campeonato.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "estadios")
public class Estadio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
}
