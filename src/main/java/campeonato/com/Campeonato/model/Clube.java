package campeonato.com.Campeonato.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name= "clubes")
public class Clube {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    @Column(name="estado")
    private String uf;

    @Column(name="data_criacao")
    private LocalDate dataCriacao;
    @Column(name="ativo")
    private Boolean status;
}