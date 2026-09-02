package br.com.api.astraai.historico.models;

import br.com.api.astraai.rectenna.models.Rectenna;
import br.com.api.astraai.satelite.models.Satelite;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "AST_HISTORICO_DESVIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoDesvio {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AST_DESVIO")
    @SequenceGenerator(name = "SEQ_AST_DESVIO", sequenceName = "SEQ_AST_DESVIO", allocationSize = 1)
    private Long idDesvio;

    @ManyToOne
    @JoinColumn(name = "ID_SATELITE", nullable = false)
    private Satelite satelite;

    @ManyToOne
    @JoinColumn(name = "ID_RECTENNA_ORIGEM", nullable = false)
    private Rectenna rectennaOrigem;

    @ManyToOne
    @JoinColumn(name = "ID_RECTENNA_DESTINO", nullable = false)
    private Rectenna rectennaDestino;

    private LocalDateTime dataHoraManobra;
}
