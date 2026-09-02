package br.com.api.astraai.rectenna.models;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "AST_RECTENNA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rectenna {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ast_rectenna")
    @SequenceGenerator(name = "seq_ast_rectenna", sequenceName = "SEQ_AST_RECTENNA", allocationSize = 1)
    private Long idRectenna;
    private String nomeSubestacao;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal capacidadeSuportadaGwh;
    private String statusOperacional;
}

