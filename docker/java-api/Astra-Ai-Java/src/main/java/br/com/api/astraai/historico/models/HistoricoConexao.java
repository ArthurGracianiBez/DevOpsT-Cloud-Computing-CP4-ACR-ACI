package br.com.api.astraai.historico.models;

import br.com.api.astraai.rectenna.models.Rectenna;
import br.com.api.astraai.satelite.models.Satelite;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "AST_HISTORICO_CONEXAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoConexao {

    @EmbeddedId
    private HistoricoConexaoId id;

    @ManyToOne
    @MapsId("idSatelite")
    @JoinColumn(name = "ID_SATELITE")
    private Satelite satelite;

    @ManyToOne
    @MapsId("idRectenna")
    @JoinColumn(name = "ID_RECTENNA")
    private Rectenna rectenna;

    private String statusConexao;
}
