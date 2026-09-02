package br.com.api.astraai.satelite.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "AST_SATELITE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Satelite {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ast_satelite")
    @SequenceGenerator(name = "seq_ast_satelite", sequenceName = "SEQ_AST_SATELITE", allocationSize = 1)
    private Long idSatelite;
    private String nomeSatelite;
    private String statusOperacional;
    private BigDecimal eficienciaPaineis;
    private BigDecimal capacidadeMaxGw;
}
