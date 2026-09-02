package br.com.api.astraai.historico.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class HistoricoConexaoId implements Serializable {
    private Long idSatelite;
    private Long idRectenna;
    private LocalDateTime dataHoraConexao;
}
