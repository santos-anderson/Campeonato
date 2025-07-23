package campeonato.com.Campeonato.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "estadios")
public class Estadio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="nome")
    private String nome;
    @Column(name="cep")
    private String cep;
    @Column(name="logradouro")
    private String logradouro;
    @Column(name="bairro")
    private String bairro;
    @Column(name="Localidade")
    private String localidade;
    @Column(name="uf")
    private String uf;
}
